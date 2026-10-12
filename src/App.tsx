import React, { useState, useEffect, useRef } from 'react';
import { ChevronsDown } from 'lucide-react';
import {
  ChatMessage,
  MessageAttachmentType,
  SharedContentItem,
  ActiveCallState,
  PendingMediaPreview,
} from './types/chat';
import {
  loadStoredMessages,
  saveStoredMessages,
  getFormattedCurrentTime,
} from './utils/storageHelper';
import {
  VoiceAudioEngine,
  resampleWaveform,
} from './utils/audioHelper';
import { ChatTopBar } from './components/ChatTopBar';
import { ChatBackground } from './components/ChatBackground';
import {
  EncryptionNoticeBanner,
  DatePillBadge,
  MessageBubbleItem,
} from './components/MessageBubble';
import { MessageComposer } from './components/MessageComposer';
import { AttachmentMenu } from './components/AttachmentMenu';
import { AudioPickerDialog } from './components/AudioPickerDialog';
import { MediaPreviewDialog } from './components/MediaPreviewDialog';
import { ImageViewerDialog } from './components/ImageViewerDialog';
import { SharedMediaStageBar } from './components/SharedMediaStageBar';
import { AudioCallScreen, VideoCallScreen } from './components/CallScreen';
import { EmojiPickerView } from './components/EmojiPicker';

export const App: React.FC = () => {
  const [messages, setMessages] = useState<ChatMessage[]>(() => loadStoredMessages());
  const [inputText, setInputText] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [stagedSharedItems, setStagedSharedItems] = useState<SharedContentItem[]>([]);

  // Dialog & screen states
  const [activeCallState, setActiveCallState] = useState<ActiveCallState>('NONE');
  const [showAttachmentMenu, setShowAttachmentMenu] = useState(false);
  const [showAudioPickerDialog, setShowAudioPickerDialog] = useState(false);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [pendingMediaPreview, setPendingMediaPreview] = useState<PendingMediaPreview | null>(null);
  const [viewerImagePath, setViewerImagePath] = useState<string | null>(null);
  const [showScrollToBottom, setShowScrollToBottom] = useState(false);

  // Voice recording state
  const [isRecording, setIsRecording] = useState(false);
  const [isRecordingLocked, setIsRecordingLocked] = useState(false);
  const [isRecordingPaused, setIsRecordingPaused] = useState(false);
  const [recordingDurationSeconds, setRecordingDurationSeconds] = useState(0);
  const [liveWaveformSamples, setLiveWaveformSamples] = useState<number[]>([]);

  // Playback state
  const [currentlyPlayingAudioId, setCurrentlyPlayingAudioId] = useState<string | null>(null);
  const [audioPlaybackProgress, setAudioPlaybackProgress] = useState(0);

  // Hidden file inputs
  const galleryInputRef = useRef<HTMLInputElement | null>(null);
  const cameraInputRef = useRef<HTMLInputElement | null>(null);
  const docInputRef = useRef<HTMLInputElement | null>(null);

  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const scrollContainerRef = useRef<HTMLDivElement | null>(null);
  const audioEngineRef = useRef<VoiceAudioEngine>(new VoiceAudioEngine());
  const recTimerRef = useRef<NodeJS.Timeout | null>(null);

  // Save messages whenever updated
  useEffect(() => {
    saveStoredMessages(messages);
  }, [messages]);

  // Scroll to bottom helper
  const scrollToBottom = (smooth = true) => {
    messagesEndRef.current?.scrollIntoView({ behavior: smooth ? 'smooth' : 'auto' });
  };

  // Auto reply logic matching MainActivity.kt
  const triggerAutoReply = (topic: string) => {
    setTimeout(() => {
      setIsTyping(true);
      setTimeout(() => {
        setIsTyping(false);
        const lower = topic.toLowerCase();
        let replyText = 'Thank you for contacting Apex Customer Care! An agent will respond shortly.';

        if (lower.includes('photo') || lower.includes('image')) {
          replyText =
            'We have received your image attachment. Our technical specialist is inspecting it now.';
        } else if (lower.includes('video')) {
          replyText =
            'Thank you for sharing the video clip. A customer representative is reviewing it.';
        } else if (lower.includes('document') || lower.includes('file')) {
          replyText =
            'Thank you for providing the document. It has been queued for verification.';
        } else if (lower.includes('voice') || lower.includes('audio')) {
          replyText = 'Voice note received. A support agent is listening to your message.';
        }

        const replyMsg: ChatMessage = {
          id: `agent_${Date.now()}`,
          text: replyText,
          timestamp: getFormattedCurrentTime(),
          isOutgoing: false,
          status: 'READ',
          attachmentType: 'NONE',
          waveform: [],
        };

        setMessages((prev) => [...prev, replyMsg]);
        setTimeout(() => scrollToBottom(true), 50);
      }, 1300);
    }, 700);
  };

  const appendUserMessage = (msg: ChatMessage, triggerTopic = '') => {
    setMessages((prev) => [...prev, msg]);
    setTimeout(() => scrollToBottom(true), 50);
    if (triggerTopic) {
      triggerAutoReply(triggerTopic);
    }
  };

  // Send typed text message or staged items
  const handleSendMessage = () => {
    const textToSend = inputText.trim();

    // If staged system shared items exist, dispatch them first
    if (stagedSharedItems.length > 0) {
      const itemsToSend = [...stagedSharedItems];
      setStagedSharedItems([]);

      itemsToSend.forEach((shared, idx) => {
        const caption = idx === itemsToSend.length - 1 ? textToSend : '';
        const newMsg: ChatMessage = {
          id: `share_${Date.now()}_${idx}`,
          text: caption,
          timestamp: getFormattedCurrentTime(),
          isOutgoing: true,
          status: 'READ',
          attachmentType: shared.type,
          mediaPath: shared.dataUrl,
          thumbnailPath: shared.thumbnailUrl,
          attachmentName: shared.fileName,
          attachmentSize: shared.sizeOrDuration,
          audioDuration: shared.type === 'AUDIO' ? shared.sizeOrDuration : undefined,
          waveform: [],
        };
        appendUserMessage(newMsg);
      });

      setInputText('');
      setShowEmojiPicker(false);
      triggerAutoReply('shared content');
      return;
    }

    if (!textToSend) return;

    const newMsg: ChatMessage = {
      id: `user_${Date.now()}`,
      text: textToSend,
      timestamp: getFormattedCurrentTime(),
      isOutgoing: true,
      status: 'READ',
      attachmentType: 'NONE',
      waveform: [],
    };

    appendUserMessage(newMsg, textToSend);
    setInputText('');
    setShowEmojiPicker(false);
  };

  // Voice recording handlers
  const handleStartRecording = async (locked = false) => {
    setIsRecording(true);
    setIsRecordingLocked(locked);
    setIsRecordingPaused(false);
    setRecordingDurationSeconds(0);
    setLiveWaveformSamples([]);

    // Timer tick
    if (recTimerRef.current) clearInterval(recTimerRef.current);
    recTimerRef.current = setInterval(() => {
      setRecordingDurationSeconds((sec) => sec + 1);
    }, 1000);

    await audioEngineRef.current.startRecording((amplitude) => {
      setLiveWaveformSamples((prev) => {
        const next = [...prev, amplitude];
        return next.length > 100 ? next.slice(-100) : next;
      });
    });
  };

  const handleLockRecording = () => {
    setIsRecordingLocked(true);
  };

  const handleTogglePauseRecording = () => {
    if (isRecordingPaused) {
      audioEngineRef.current.resumeRecording();
      setIsRecordingPaused(false);
      if (recTimerRef.current) clearInterval(recTimerRef.current);
      recTimerRef.current = setInterval(() => {
        setRecordingDurationSeconds((sec) => sec + 1);
      }, 1000);
    } else {
      audioEngineRef.current.pauseRecording();
      setIsRecordingPaused(true);
      if (recTimerRef.current) clearInterval(recTimerRef.current);
    }
  };

  const handleCancelRecording = () => {
    if (recTimerRef.current) clearInterval(recTimerRef.current);
    audioEngineRef.current.cancelRecording();
    setIsRecording(false);
    setIsRecordingLocked(false);
    setIsRecordingPaused(false);
    setRecordingDurationSeconds(0);
    setLiveWaveformSamples([]);
  };

  const handleSendRecording = async () => {
    if (recTimerRef.current) clearInterval(recTimerRef.current);
    const durationSec = recordingDurationSeconds;
    const mins = Math.floor(durationSec / 60);
    const secs = durationSec % 60;
    const durationLabel = `${mins}:${secs < 10 ? '0' : ''}${secs}`;

    const computedWaveform = resampleWaveform(liveWaveformSamples, 30);
    const result = await audioEngineRef.current.stopRecording();

    setIsRecording(false);
    setIsRecordingLocked(false);
    setIsRecordingPaused(false);
    setRecordingDurationSeconds(0);
    setLiveWaveformSamples([]);

    const newMsg: ChatMessage = {
      id: `voice_${Date.now()}`,
      text: '',
      timestamp: getFormattedCurrentTime(),
      isOutgoing: true,
      status: 'READ',
      attachmentType: 'VOICE_NOTE',
      mediaPath: result.url || null,
      audioDuration: durationLabel || '0:03',
      waveform: computedWaveform,
    };

    appendUserMessage(newMsg, 'voice');
  };

  // Playback handlers
  const handleTogglePlayAudio = (path: string, id: string) => {
    if (currentlyPlayingAudioId === id) {
      audioEngineRef.current.stopPlayback();
      setCurrentlyPlayingAudioId(null);
      setAudioPlaybackProgress(0);
      return;
    }

    setCurrentlyPlayingAudioId(id);
    setAudioPlaybackProgress(0);

    audioEngineRef.current.playAudio(
      id,
      path,
      (progress) => {
        setAudioPlaybackProgress(progress);
      },
      () => {
        setCurrentlyPlayingAudioId(null);
        setAudioPlaybackProgress(0);
      }
    );
  };

  const handleSeekAudio = (_path: string, id: string, progress: number) => {
    if (currentlyPlayingAudioId === id) {
      audioEngineRef.current.seek(progress);
      setAudioPlaybackProgress(progress);
    }
  };

  // Attachment picks
  const handleGalleryFilePicked = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const isVideo = file.type.startsWith('video/');
      const url = URL.createObjectURL(file);
      setPendingMediaPreview({
        fileOrUrl: url,
        isVideo,
        duration: isVideo ? '0:15' : undefined,
        fileName: file.name,
      });
    }
    e.target.value = '';
  };

  const handleCameraCapture = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      setPendingMediaPreview({
        fileOrUrl: url,
        isVideo: false,
        fileName: file.name,
      });
    }
    e.target.value = '';
  };

  const handleDocumentPicked = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const url = URL.createObjectURL(file);
      const sizeStr = file.size > 1024 * 1024
        ? `${(file.size / (1024 * 1024)).toFixed(1)} MB`
        : `${Math.round(file.size / 1024)} KB`;

      const newMsg: ChatMessage = {
        id: `doc_${Date.now()}`,
        text: '',
        timestamp: getFormattedCurrentTime(),
        isOutgoing: true,
        status: 'READ',
        attachmentType: 'DOCUMENT',
        mediaPath: url,
        attachmentName: file.name,
        attachmentSize: sizeStr,
        waveform: [],
      };
      appendUserMessage(newMsg, 'document');
    }
    e.target.value = '';
  };

  const handleAudioPicked = (
    _file: File | null,
    fileName: string,
    sizeOrDuration: string,
    dataUrl: string
  ) => {
    const newMsg: ChatMessage = {
      id: `audio_${Date.now()}`,
      text: '',
      timestamp: getFormattedCurrentTime(),
      isOutgoing: true,
      status: 'READ',
      attachmentType: 'AUDIO',
      mediaPath: dataUrl,
      attachmentName: fileName,
      attachmentSize: sizeOrDuration,
      audioDuration: sizeOrDuration,
      waveform: [],
    };
    appendUserMessage(newMsg, 'audio');
  };

  // Scroll detection
  const handleScroll = () => {
    if (scrollContainerRef.current) {
      const { scrollTop, scrollHeight, clientHeight } = scrollContainerRef.current;
      const distanceFromBottom = scrollHeight - scrollTop - clientHeight;
      setShowScrollToBottom(distanceFromBottom > 120);
    }
  };

  // Active Calling View Rendering
  if (activeCallState === 'AUDIO') {
    return <AudioCallScreen onEndCall={() => setActiveCallState('NONE')} />;
  }

  if (activeCallState === 'VIDEO') {
    return <VideoCallScreen onEndCall={() => setActiveCallState('NONE')} />;
  }

  return (
    <div className="flex flex-col h-screen w-full overflow-hidden bg-[#e5ddd5] text-[#111B21]">
      {/* WhatsApp Green Top Header */}
      <ChatTopBar
        isTyping={isTyping}
        onAudioCallClick={() => setActiveCallState('AUDIO')}
        onVideoCallClick={() => setActiveCallState('VIDEO')}
        onBackClick={() => {
          // Toast representation
        }}
      />

      {/* WhatsApp Chat Canvas Wallpaper */}
      <ChatBackground>
        {/* Messages Stream */}
        <div
          ref={scrollContainerRef}
          onScroll={handleScroll}
          className="flex-1 overflow-y-auto px-1 sm:px-2 py-2 flex flex-col space-y-1"
          data-testid="chat_messages_list"
        >
          {/* Encryption Banner */}
          <EncryptionNoticeBanner />

          {/* Date Pill: Today */}
          <DatePillBadge dateText="Today" />

          {/* Chat Messages */}
          {messages.map((message) => {
            const isPlaying = currentlyPlayingAudioId === message.id;
            return (
              <MessageBubbleItem
                key={message.id}
                message={message}
                isAudioPlaying={isPlaying}
                audioPlaybackProgress={isPlaying ? audioPlaybackProgress : 0}
                onImageClick={(path) => setViewerImagePath(path)}
                onVideoClick={(path) => setViewerImagePath(path)}
                onAudioTogglePlay={handleTogglePlayAudio}
                onSeekAudio={handleSeekAudio}
              />
            );
          })}

          <div ref={messagesEndRef} className="h-2 flex-shrink-0" />
        </div>

        {/* Staged Shared Media Bar (for multiple pending shared files) */}
        <SharedMediaStageBar
          items={stagedSharedItems}
          onRemoveItem={(item) =>
            setStagedSharedItems((prev) => prev.filter((i) => i.id !== item.id))
          }
          onClearAll={() => setStagedSharedItems([])}
        />

        {/* Message Composer & Voice Recording Panel */}
        <MessageComposer
          text={inputText}
          onTextChanged={setInputText}
          onSendMessage={handleSendMessage}
          onEmojiClick={() => setShowEmojiPicker(!showEmojiPicker)}
          onAttachmentClick={() => setShowAttachmentMenu(true)}
          onCameraClick={() => cameraInputRef.current?.click()}
          isRecording={isRecording}
          isRecordingLocked={isRecordingLocked}
          isRecordingPaused={isRecordingPaused}
          recordingDurationSeconds={recordingDurationSeconds}
          liveWaveform={liveWaveformSamples}
          onStartRecording={handleStartRecording}
          onLockRecording={handleLockRecording}
          onTogglePauseRecording={handleTogglePauseRecording}
          onCancelRecording={handleCancelRecording}
          onSendRecording={handleSendRecording}
          hasPendingAttachments={stagedSharedItems.length > 0}
        />

        {/* Integrated Emoji Picker */}
        {showEmojiPicker && (
          <EmojiPickerView
            onEmojiSelected={(emoji) => setInputText((prev) => prev + emoji)}
            onBackspaceClick={() => setInputText((prev) => prev.slice(0, -1))}
          />
        )}
      </ChatBackground>

      {/* Floating Scroll to Bottom Button */}
      {showScrollToBottom && (
        <button
          onClick={() => scrollToBottom(true)}
          aria-label="Scroll to bottom"
          className="fixed right-4 bottom-20 w-10 h-10 rounded-full bg-white shadow-md border border-gray-100 flex items-center justify-center text-[#54656F] hover:bg-gray-50 active:scale-95 transition cursor-pointer z-30"
          data-testid="scroll_to_bottom_button"
        >
          <ChevronsDown className="w-5 h-5" />
        </button>
      )}

      {/* Pre-Send Media Preview Dialog (Images & Videos) */}
      <MediaPreviewDialog
        preview={pendingMediaPreview}
        onSend={(caption) => {
          if (!pendingMediaPreview) return;
          const newMsg: ChatMessage = {
            id: `media_${Date.now()}`,
            text: caption,
            timestamp: getFormattedCurrentTime(),
            isOutgoing: true,
            status: 'READ',
            attachmentType: pendingMediaPreview.isVideo ? 'VIDEO' : 'IMAGE',
            mediaPath: pendingMediaPreview.fileOrUrl,
            thumbnailPath: pendingMediaPreview.thumbnailUrl,
            audioDuration: pendingMediaPreview.duration,
            attachmentName: pendingMediaPreview.fileName,
            waveform: [],
          };
          appendUserMessage(newMsg, pendingMediaPreview.isVideo ? 'video' : 'photo');
          setPendingMediaPreview(null);
        }}
        onCancel={() => setPendingMediaPreview(null)}
      />

      {/* Fullscreen Photo Lightbox */}
      <ImageViewerDialog
        imagePath={viewerImagePath}
        onDismiss={() => setViewerImagePath(null)}
      />

      {/* Attachment Menu Bottom Sheet */}
      <AttachmentMenu
        isOpen={showAttachmentMenu}
        onDismiss={() => setShowAttachmentMenu(false)}
        onPickGallery={() => galleryInputRef.current?.click()}
        onCaptureCamera={() => cameraInputRef.current?.click()}
        onPickDocuments={() => docInputRef.current?.click()}
        onPickAudio={() => setShowAudioPickerDialog(true)}
      />

      {/* Audio Picker Dialog */}
      <AudioPickerDialog
        isOpen={showAudioPickerDialog}
        onDismiss={() => setShowAudioPickerDialog(false)}
        onSelectAudio={handleAudioPicked}
      />

      {/* Hidden File Inputs */}
      <input
        ref={galleryInputRef}
        type="file"
        accept="image/*,video/*"
        className="hidden"
        onChange={handleGalleryFilePicked}
      />
      <input
        ref={cameraInputRef}
        type="file"
        accept="image/*"
        capture="environment"
        className="hidden"
        onChange={handleCameraCapture}
      />
      <input
        ref={docInputRef}
        type="file"
        accept="*/*,.pdf,.doc,.docx,.txt"
        className="hidden"
        onChange={handleDocumentPicked}
      />
    </div>
  );
};
export default App;
