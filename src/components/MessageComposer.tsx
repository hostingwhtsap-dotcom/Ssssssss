import React, { useState, useRef, useEffect } from 'react';
import {
  Smile,
  Paperclip,
  Camera,
  Mic,
  Send,
  Trash2,
  Pause,
  Play,
  Lock,
  ChevronLeft,
  ChevronUp,
} from 'lucide-react';

interface MessageComposerProps {
  text: string;
  onTextChanged: (text: string) => void;
  onSendMessage: () => void;
  onEmojiClick: () => void;
  onAttachmentClick: () => void;
  onCameraClick: () => void;

  // Voice recording props
  isRecording: boolean;
  isRecordingLocked: boolean;
  isRecordingPaused: boolean;
  recordingDurationSeconds: number;
  liveWaveform: number[];
  onStartRecording: (locked: boolean) => void;
  onLockRecording: () => void;
  onTogglePauseRecording: () => void;
  onCancelRecording: () => void;
  onSendRecording: () => void;
  hasPendingAttachments?: boolean;
}

export const MessageComposer: React.FC<MessageComposerProps> = ({
  text,
  onTextChanged,
  onSendMessage,
  onEmojiClick,
  onAttachmentClick,
  onCameraClick,
  isRecording,
  isRecordingLocked,
  isRecordingPaused,
  recordingDurationSeconds,
  liveWaveform,
  onStartRecording,
  onLockRecording,
  onTogglePauseRecording,
  onCancelRecording,
  onSendRecording,
  hasPendingAttachments = false,
}) => {
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const touchStartRef = useRef<{ x: number; y: number; time: number } | null>(null);
  const inputRef = useRef<HTMLInputElement | null>(null);

  const canSend = text.trim().length > 0 || hasPendingAttachments;

  // Format recording timer: "0:06"
  const mins = Math.floor(recordingDurationSeconds / 60);
  const secs = recordingDurationSeconds % 60;
  const timerStr = `${mins}:${secs < 10 ? '0' : ''}${secs}`;

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      if (canSend) {
        onSendMessage();
      }
    }
  };

  // Pointer / Touch gestures for press-and-hold microphone recording
  const handlePointerDown = (e: React.PointerEvent) => {
    e.currentTarget.setPointerCapture(e.pointerId);
    touchStartRef.current = {
      x: e.clientX,
      y: e.clientY,
      time: Date.now(),
    };
    setDragOffset({ x: 0, y: 0 });
    onStartRecording(false);
  };

  const handlePointerMove = (e: React.PointerEvent) => {
    if (!touchStartRef.current || !isRecording || isRecordingLocked) return;

    const dx = e.clientX - touchStartRef.current.x;
    const dy = e.clientY - touchStartRef.current.y;
    setDragOffset({ x: dx, y: dy });

    // Slide up to lock threshold (~60px up)
    if (dy < -60) {
      onLockRecording();
      touchStartRef.current = null;
    }
    // Slide left to cancel threshold (~80px left)
    else if (dx < -80) {
      onCancelRecording();
      touchStartRef.current = null;
    }
  };

  const handlePointerUp = () => {
    if (!touchStartRef.current || !isRecording || isRecordingLocked) return;

    const elapsed = Date.now() - touchStartRef.current.time;
    touchStartRef.current = null;
    setDragOffset({ x: 0, y: 0 });

    if (elapsed < 350) {
      // Short tap: lock hands-free recording so user can record comfortably
      onLockRecording();
    } else {
      // Long press release without locking: send immediately!
      onSendRecording();
    }
  };

  // Mode 1: WhatsApp Locked Hands-Free Recording UI (matching Reference Screenshot 1)
  if (isRecording && isRecordingLocked) {
    return (
      <div
        className="w-full bg-white rounded-t-3xl shadow-xl px-5 py-3 select-none border-t border-gray-100 z-20"
        data-testid="voice_recording_panel"
      >
        {/* Top Row: Timer + Live Waveform + View Once "(1)" button */}
        <div className="flex items-center justify-between gap-3 mb-4">
          {/* Timer with blinking dot */}
          <div className="flex items-center gap-2">
            <span
              className={`w-2.5 h-2.5 rounded-full bg-[#E91E63] ${
                isRecordingPaused ? 'opacity-50' : 'animate-pulse'
              }`}
            />
            <span
              className="text-[19px] font-semibold text-[#111B21] tracking-tight"
              data-testid="recording_timer_text"
            >
              {timerStr}
            </span>
          </div>

          {/* Live Waveform Track */}
          <div
            className="flex-1 flex items-center justify-center h-7 px-2"
            data-testid="recording_waveform_track"
          >
            <LiveWaveformTrack bars={liveWaveform} isPaused={isRecordingPaused} />
          </div>

          {/* View Once "(1)" icon */}
          <div className="w-7 h-7 rounded-full border-[1.5px] border-[#111B21] flex items-center justify-center font-bold text-[13px] text-[#111B21] flex-shrink-0">
            1
          </div>
        </div>

        {/* Bottom Row: Trash icon + [ Pause / Resume ] pill + Send icon */}
        <div className="flex items-center justify-between">
          {/* Delete Button (Soft pink circular button) */}
          <button
            onClick={onCancelRecording}
            aria-label="Delete recording"
            className="w-[50px] h-[50px] rounded-full bg-[#FCE4EC] flex items-center justify-center text-[#E91E63] hover:bg-[#F8BBD0] active:scale-95 transition cursor-pointer"
            data-testid="cancel_recording_button"
          >
            <Trash2 className="w-6 h-6" />
          </button>

          {/* Center Pause/Resume pill */}
          <button
            onClick={onTogglePauseRecording}
            aria-label={isRecordingPaused ? 'Resume recording' : 'Pause recording'}
            className="h-[46px] w-[160px] rounded-full bg-[#F2F4F5] hover:bg-[#E4E6EB] active:scale-98 transition flex items-center justify-center gap-2 text-[#111B21] font-medium text-[15px] cursor-pointer"
            data-testid="pause_resume_button"
          >
            {isRecordingPaused ? (
              <>
                <Play className="w-5 h-5 fill-current" />
                <span>Resume</span>
              </>
            ) : (
              <>
                <Pause className="w-5 h-5 fill-current" />
                <span>Pause</span>
              </>
            )}
          </button>

          {/* Send Recording Button */}
          <button
            onClick={onSendRecording}
            aria-label="Send voice note"
            className="w-[50px] h-[50px] rounded-full bg-[#E91E63] hover:bg-[#D81B60] active:scale-95 transition flex items-center justify-center text-white cursor-pointer shadow-md"
            data-testid="send_voice_note_button"
          >
            <Send className="w-5 h-5 ml-0.5" />
          </button>
        </div>
      </div>
    );
  }

  // Mode 2: Press & Hold Recording Active (Holding & sliding)
  if (isRecording && !isRecordingLocked) {
    return (
      <div className="relative w-full px-2 py-1.5 flex items-center gap-1.5 select-none z-20">
        {/* Slide-Up Lock Pill Indicator above microphone */}
        <div
          className="absolute right-3.5 -top-16 bg-white rounded-2xl shadow-lg px-2.5 py-2 flex flex-col items-center gap-1 text-[#54656F] border border-gray-100 animate-bounce"
          data-testid="slide_to_lock_indicator"
        >
          <Lock className="w-4 h-4" />
          <ChevronUp className="w-4 h-4 text-[#E91E63]" />
        </div>

        {/* Capsule: Recording dot + Timer + Live Waveform + Slide to cancel */}
        <div className="flex-1 h-12 bg-white rounded-full shadow-sm flex items-center px-4 gap-2.5">
          <span className="w-2.5 h-2.5 rounded-full bg-[#E91E63] animate-pulse flex-shrink-0" />
          <span className="text-[15px] font-semibold text-[#111B21]" data-testid="recording_timer_text">
            {timerStr}
          </span>
          <div className="flex-1 flex items-center justify-center h-6 overflow-hidden">
            <LiveWaveformTrack bars={liveWaveform} isPaused={false} />
          </div>
          <div
            className="flex items-center text-[#54656F] text-[13px] font-medium transition-transform"
            style={{ transform: `translateX(${Math.min(0, dragOffset.x)}px)` }}
          >
            <ChevronLeft className="w-4 h-4" />
            <span>Slide to cancel</span>
          </div>
        </div>

        {/* Held Microphone Button */}
        <div
          className="w-12 h-12 rounded-full bg-[#E91E63] text-white flex items-center justify-center shadow-md scale-110 flex-shrink-0"
          data-testid="mic_button_held"
        >
          <Mic className="w-6 h-6" />
        </div>
      </div>
    );
  }

  // Mode 3: Normal WhatsApp Composer (Idle State)
  return (
    <div className="w-full px-1.5 sm:px-2 py-1.5 flex items-center gap-1.5 select-none z-20">
      {/* Left Capsule: [😀] [Message] [📎] [📷] */}
      <div className="flex-1 h-12 bg-white rounded-full shadow-xs flex items-center px-1.5 sm:px-2 border border-gray-200/50">
        {/* Emoji Button */}
        <button
          onClick={onEmojiClick}
          aria-label="Emoji picker"
          className="w-9 h-9 rounded-full flex items-center justify-center text-[#667781] hover:bg-gray-100 transition cursor-pointer flex-shrink-0"
          data-testid="emoji_icon_button"
        >
          <Smile className="w-6 h-6" />
        </button>

        {/* Text Input Field */}
        <input
          ref={inputRef}
          type="text"
          value={text}
          onChange={(e) => onTextChanged(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Message"
          className="flex-1 min-w-0 px-2 py-2 text-[16px] text-[#111B21] placeholder-[#8696A0] bg-transparent focus:outline-none"
          data-testid="message_input_field"
        />

        {/* Attachment Paperclip */}
        <button
          onClick={onAttachmentClick}
          aria-label="Attachments"
          className="w-9 h-9 rounded-full flex items-center justify-center text-[#667781] hover:bg-gray-100 transition cursor-pointer flex-shrink-0 -rotate-45"
          data-testid="attachment_icon_button"
        >
          <Paperclip className="w-5 h-5" />
        </button>

        {/* Dedicated Camera Icon (visible when text is empty and no staged attachments) */}
        {!canSend && (
          <button
            onClick={onCameraClick}
            aria-label="Camera"
            className="w-9 h-9 rounded-full flex items-center justify-center text-[#667781] hover:bg-gray-100 transition cursor-pointer flex-shrink-0"
            data-testid="camera_icon_button"
          >
            <Camera className="w-5 h-5" />
          </button>
        )}
      </div>

      {/* Right Action Button (48px Pink Button): Send OR Mic */}
      {canSend ? (
        <button
          onClick={onSendMessage}
          aria-label="Send message"
          className="w-12 h-12 rounded-full bg-[#E91E63] hover:bg-[#D81B60] active:scale-95 transition flex items-center justify-center text-white shadow-md flex-shrink-0 cursor-pointer"
          data-testid="send_button"
        >
          <Send className="w-5 h-5 ml-0.5" />
        </button>
      ) : (
        <button
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={handlePointerUp}
          onPointerCancel={handlePointerUp}
          onClick={() => onStartRecording(true)}
          aria-label="Record voice note"
          className="w-12 h-12 rounded-full bg-[#E91E63] hover:bg-[#D81B60] active:scale-95 transition flex items-center justify-center text-white shadow-md flex-shrink-0 cursor-pointer touch-none"
          data-testid="mic_button"
        >
          <Mic className="w-6 h-6" />
        </button>
      )}
    </div>
  );
};

// Renders real-time live audio waveform bars based on microphone amplitude levels
const LiveWaveformTrack: React.FC<{ bars: number[]; isPaused: boolean }> = ({
  bars,
  isPaused,
}) => {
  const targetCount = 22;
  const recentBars =
    bars.length === 0
      ? Array(targetCount).fill(0.12)
      : bars.length <= targetCount
      ? [...Array(targetCount - bars.length).fill(0.12), ...bars]
      : bars.slice(-targetCount);

  return (
    <div className="flex items-center gap-[2px]">
      {recentBars.map((amp, idx) => {
        const height = isPaused ? 5 : Math.max(4, Math.min(24, 4 + amp * 20));
        return (
          <div
            key={idx}
            style={{ height: `${height}px` }}
            className="w-[2.5px] rounded-full bg-[#54656F] transition-all duration-75"
          />
        );
      })}
    </div>
  );
};
