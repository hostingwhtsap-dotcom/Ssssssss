import React from 'react';
import {
  Lock,
  Play,
  Pause,
  Mic,
  FileText,
  Music,
  Share2,
  Check,
  CheckCheck,
} from 'lucide-react';
import { ChatMessage, MessageStatus } from '../types/chat';

export const EncryptionNoticeBanner: React.FC = () => {
  return (
    <div className="w-full flex justify-center px-4 py-2 my-1">
      <div className="bg-[#FFF5C4] text-[#54656F] text-[11.5px] leading-snug rounded-lg px-3 py-1.5 shadow-xs max-w-[340px] text-center flex items-center justify-center gap-1.5 border border-amber-200/50">
        <Lock className="w-3 h-3 flex-shrink-0 text-[#54656F]" />
        <span>
          Messages and calls are end-to-end encrypted. No one outside of this chat, not even the company, can read, listen to, or share them.
        </span>
      </div>
    </div>
  );
};

export const DatePillBadge: React.FC<{ dateText?: string }> = ({ dateText = 'Today' }) => {
  return (
    <div className="w-full flex justify-center py-1.5">
      <div className="bg-white/95 text-[#54656F] text-[11.8px] font-medium rounded-lg px-3 py-1 shadow-xs border border-gray-200/50">
        {dateText}
      </div>
    </div>
  );
};

interface MessageBubbleItemProps {
  message: ChatMessage;
  isAudioPlaying: boolean;
  audioPlaybackProgress: number;
  onImageClick: (path: string) => void;
  onVideoClick: (path: string) => void;
  onAudioTogglePlay: (path: string, id: string) => void;
  onSeekAudio: (path: string, id: string, progress: number) => void;
}

export const MessageBubbleItem: React.FC<MessageBubbleItemProps> = ({
  message,
  isAudioPlaying,
  audioPlaybackProgress,
  onImageClick,
  onVideoClick,
  onAudioTogglePlay,
  onSeekAudio,
}) => {
  const isOutgoing = message.isOutgoing;
  const isVoiceNote = message.attachmentType === 'VOICE_NOTE';
  const isImage = message.attachmentType === 'IMAGE';
  const isVideo = message.attachmentType === 'VIDEO';

  // Bubble color matching Android implementation:
  // Outgoing voice notes use WhatsAppPinkAccent (#E91E63)
  // Outgoing normal uses #D9FDD3
  // Incoming uses #FFFFFF
  const bubbleBg = isVoiceNote && isOutgoing
    ? 'bg-[#E91E63] text-white'
    : isOutgoing
    ? 'bg-[#D9FDD3] text-[#111B21]'
    : 'bg-white text-[#111B21]';

  const bubbleRadius = isOutgoing
    ? 'rounded-tl-2xl rounded-tr-xs rounded-bl-2xl rounded-br-2xl'
    : 'rounded-tl-xs rounded-tr-2xl rounded-bl-2xl rounded-br-2xl';

  return (
    <div
      className={`w-full flex items-center my-0.5 px-2 sm:px-3 ${
        isOutgoing ? 'justify-end pl-10' : 'justify-start pr-10'
      }`}
      data-testid={isOutgoing ? `outgoing_bubble_${message.id}` : `incoming_bubble_${message.id}`}
    >
      {/* Quick forward button for outgoing image/video */}
      {isOutgoing && (isImage || isVideo) && (
        <button
          type="button"
          aria-label="Forward"
          className="mr-2 w-[34px] h-[34px] rounded-full bg-white flex items-center justify-center text-[#54656F] shadow-xs hover:bg-gray-50 active:scale-95 transition cursor-pointer"
        >
          <Share2 className="w-4 h-4 scale-x-[-1]" />
        </button>
      )}

      {/* Bubble Surface */}
      <div
        className={`relative max-w-[300px] sm:max-w-[330px] shadow-xs select-text overflow-hidden ${bubbleBg} ${bubbleRadius}`}
      >
        {isVoiceNote ? (
          <VoiceNoteBubble
            message={message}
            isOutgoing={isOutgoing}
            isPlaying={isAudioPlaying}
            progress={audioPlaybackProgress}
            onTogglePlay={() => onAudioTogglePlay(message.mediaPath || '', message.id)}
            onSeek={(p) => onSeekAudio(message.mediaPath || '', message.id, p)}
          />
        ) : isImage ? (
          <ImageBubble
            message={message}
            onImageClick={() => onImageClick(message.mediaPath || '')}
          />
        ) : isVideo ? (
          <VideoBubble
            message={message}
            onVideoClick={() => onVideoClick(message.mediaPath || '')}
          />
        ) : (
          <div className="px-2.5 py-1.5">
            {/* Document attachment */}
            {message.attachmentType === 'DOCUMENT' && (
              <div className="flex items-center gap-2.5 py-1 mb-1">
                <div className="w-9 h-9 rounded-lg bg-[#7F66FF]/20 flex items-center justify-center flex-shrink-0">
                  <FileText className="w-5 h-5 text-[#7F66FF]" />
                </div>
                <div className="min-w-0 flex-1">
                  <div className="text-[13.5px] font-medium text-[#111B21] truncate">
                    {message.attachmentName || 'Document.pdf'}
                  </div>
                  <div className="text-[11px] text-[#667781]">
                    {message.attachmentSize || 'Document File'}
                  </div>
                </div>
              </div>
            )}

            {/* Audio File attachment (distinct from in-app voice notes) */}
            {message.attachmentType === 'AUDIO' && (
              <div className="py-1">
                <div className="inline-flex items-center gap-1 bg-[#FFECB3] text-[#E65100] text-[10.5px] font-bold px-1.5 py-0.5 rounded-sm mb-1.5">
                  <Music className="w-3 h-3" />
                  <span>Forwarded Audio</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <button
                    onClick={() => onAudioTogglePlay(message.mediaPath || '', message.id)}
                    className="w-10 h-10 rounded-full bg-[#E0663B]/20 flex items-center justify-center text-[#E0663B] hover:bg-[#E0663B]/30 transition cursor-pointer flex-shrink-0"
                  >
                    {isAudioPlaying ? (
                      <Pause className="w-5 h-5 fill-current" />
                    ) : (
                      <Play className="w-5 h-5 fill-current ml-0.5" />
                    )}
                  </button>
                  <div className="min-w-0 flex-1">
                    <div className="text-[13.5px] font-semibold text-[#111B21] truncate">
                      {message.attachmentName || 'Audio File.mp3'}
                    </div>
                    <div className="text-[11px] text-[#667781]">
                      {message.attachmentSize || 'Audio File'}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Text message */}
            {message.text && (
              <div className="text-[15px] leading-[19px] whitespace-pre-wrap break-words text-[#111B21]">
                {message.text}
              </div>
            )}

            {/* Timestamp & Status ticks */}
            <div className="flex items-center justify-end gap-1 mt-0.5 text-[10.8px] text-[#667781]">
              <span>{message.timestamp}</span>
              {isOutgoing && <TicksIcon status={message.status} tint="#53BDEB" />}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

const VoiceNoteBubble: React.FC<{
  message: ChatMessage;
  isOutgoing: boolean;
  isPlaying: boolean;
  progress: number;
  onTogglePlay: () => void;
  onSeek: (p: number) => void;
}> = ({ message, isOutgoing, isPlaying, progress, onTogglePlay, onSeek }) => {
  const contentColor = isOutgoing ? 'text-white' : 'text-[#111B21]';
  const waveformBars = message.waveform && message.waveform.length > 0
    ? message.waveform
    : Array.from({ length: 30 }, (_, i) => 0.15 + ((i % 5) * 0.1));

  const handleWaveformClick = (e: React.MouseEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const fraction = Math.max(0, Math.min(1, clickX / rect.width));
    onSeek(fraction);
  };

  const currentDurationLabel = () => {
    if (isPlaying && progress > 0) {
      const parts = (message.audioDuration || '0:03').split(':');
      const totalSecs = (parseInt(parts[0], 10) || 0) * 60 + (parseInt(parts[1], 10) || 3);
      const curSecs = Math.floor(totalSecs * progress);
      const m = Math.floor(curSecs / 60);
      const s = curSecs % 60;
      return `${m}:${s < 10 ? '0' : ''}${s}`;
    }
    return message.audioDuration || '0:03';
  };

  return (
    <div className="px-2.5 py-1.5 min-w-[250px] sm:min-w-[270px]">
      <div className="flex items-center gap-2">
        {/* Avatar with mic badge */}
        <div className="relative w-10 h-10 flex-shrink-0">
          <img
            src="/company_logo.jpg"
            alt="Avatar"
            className="w-10 h-10 rounded-full object-cover border border-white/20"
            onError={(e) => {
              (e.target as HTMLElement).style.display = 'none';
            }}
          />
          <div
            className={`absolute -bottom-0.5 -right-0.5 w-4 h-4 rounded-full flex items-center justify-center border border-white ${
              isOutgoing ? 'bg-white text-[#E91E63]' : 'bg-[#E91E63] text-white'
            }`}
          >
            <Mic className="w-2.5 h-2.5" />
          </div>
        </div>

        {/* Play/Pause Button */}
        <button
          onClick={onTogglePlay}
          aria-label={isPlaying ? 'Pause voice note' : 'Play voice note'}
          className={`w-8 h-8 rounded-full flex items-center justify-center transition active:scale-95 cursor-pointer ${contentColor}`}
          data-testid={`play_pause_voice_${message.id}`}
        >
          {isPlaying ? (
            <Pause className="w-5 h-5 fill-current" />
          ) : (
            <Play className="w-5 h-5 fill-current ml-0.5" />
          )}
        </button>

        {/* Waveform track */}
        <div
          onClick={handleWaveformClick}
          className="flex-1 flex items-center h-8 cursor-pointer gap-[2px] select-none"
        >
          <div className={`w-1 h-1 rounded-full ${isOutgoing ? 'bg-white' : 'bg-[#111B21]'}`} />
          <div className="flex-1 flex items-center justify-between h-full px-0.5">
            {waveformBars.map((amp, idx) => {
              const fraction = idx / (waveformBars.length - 1);
              const isPlayed = fraction <= progress;
              const barHeight = Math.max(4, Math.min(22, 4 + amp * 18));

              let barColor = '';
              if (isOutgoing) {
                barColor = isPlayed ? 'bg-white' : 'bg-white/40';
              } else {
                barColor = isPlayed ? 'bg-[#E91E63]' : 'bg-[#8696A0]';
              }

              return (
                <div
                  key={idx}
                  style={{ height: `${barHeight}px` }}
                  className={`w-[2.2px] rounded-full transition-all duration-75 ${barColor}`}
                />
              );
            })}
          </div>
          <div
            className={`w-1 h-1 rounded-full ${
              isOutgoing ? (progress >= 0.98 ? 'bg-white' : 'bg-white/50') : 'bg-[#8696A0]'
            }`}
          />
        </div>
      </div>

      {/* Bottom Duration & Ticks row */}
      <div className="flex items-center justify-between px-1 mt-0.5 text-[11px] font-medium">
        <span className={isOutgoing ? 'text-white/90' : 'text-[#667781]'}>
          {currentDurationLabel()}
        </span>
        <div className="flex items-center gap-1">
          <span className={isOutgoing ? 'text-white/90' : 'text-[#667781]'}>
            {message.timestamp}
          </span>
          {isOutgoing && <TicksIcon status={message.status} tint="#FFFFFF" />}
        </div>
      </div>
    </div>
  );
};

const ImageBubble: React.FC<{
  message: ChatMessage;
  onImageClick: () => void;
}> = ({ message, onImageClick }) => {
  return (
    <div
      onClick={onImageClick}
      className="relative cursor-pointer w-[250px] sm:w-[270px] overflow-hidden rounded-xl group"
    >
      <img
        src={message.mediaPath || '/company_logo.jpg'}
        alt="Photo attachment"
        className="w-full h-[210px] object-cover group-hover:scale-101 transition duration-200"
      />

      {message.text && (
        <div className="p-2 bg-[#D9FDD3] text-[#111B21] text-[14px]">
          {message.text}
        </div>
      )}

      {/* Gradient Scrim for crisp timestamp reading */}
      <div className="absolute inset-x-0 bottom-0 h-12 bg-gradient-to-t from-black/60 to-transparent pointer-events-none" />

      {/* Overlaid Timestamp & Checkmarks */}
      <div className="absolute bottom-1.5 right-2 flex items-center gap-1 text-[11px] font-medium text-white drop-shadow-sm">
        <span>{message.timestamp}</span>
        {message.isOutgoing && <TicksIcon status={message.status} tint="#FFFFFF" />}
      </div>
    </div>
  );
};

const VideoBubble: React.FC<{
  message: ChatMessage;
  onVideoClick: () => void;
}> = ({ message, onVideoClick }) => {
  return (
    <div
      onClick={onVideoClick}
      className="relative cursor-pointer w-[250px] sm:w-[270px] h-[210px] bg-[#1E2A30] overflow-hidden rounded-xl group flex items-center justify-center"
    >
      {message.thumbnailPath ? (
        <img
          src={message.thumbnailPath}
          alt="Video thumbnail"
          className="w-full h-full object-cover group-hover:scale-101 transition duration-200"
        />
      ) : (
        <div className="w-full h-full bg-[#1E2A30] flex items-center justify-center">
          <div className="text-gray-400 text-xs">Video Clip</div>
        </div>
      )}

      {/* Center 52px Play Button */}
      <div className="absolute w-[52px] h-[52px] rounded-full bg-black/60 flex items-center justify-center text-white group-hover:scale-105 active:scale-95 transition">
        <Play className="w-7 h-7 fill-current ml-1" />
      </div>

      {/* Bottom Left Duration */}
      {message.audioDuration && (
        <div className="absolute bottom-2 left-2 bg-black/65 text-white text-[11px] px-2 py-0.5 rounded-full flex items-center gap-1">
          <span>{message.audioDuration}</span>
        </div>
      )}

      {/* Overlaid Timestamp & Checkmarks */}
      <div className="absolute bottom-1.5 right-2 flex items-center gap-1 text-[11px] font-medium text-white drop-shadow-sm">
        <span>{message.timestamp}</span>
        {message.isOutgoing && <TicksIcon status={message.status} tint="#FFFFFF" />}
      </div>
    </div>
  );
};

const TicksIcon: React.FC<{ status: MessageStatus; tint?: string }> = ({
  status,
  tint = '#53BDEB',
}) => {
  if (status === 'SENT') {
    return <Check className="w-3.5 h-3.5" style={{ color: tint }} />;
  }
  return <CheckCheck className="w-3.5 h-3.5" style={{ color: tint }} />;
};
