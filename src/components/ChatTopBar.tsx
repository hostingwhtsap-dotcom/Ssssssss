import React from 'react';
import { ArrowLeft, Video, Phone } from 'lucide-react';

interface ChatTopBarProps {
  isTyping: boolean;
  onAudioCallClick: () => void;
  onVideoCallClick: () => void;
  onBackClick?: () => void;
}

export const ChatTopBar: React.FC<ChatTopBarProps> = ({
  isTyping,
  onAudioCallClick,
  onVideoCallClick,
  onBackClick,
}) => {
  return (
    <header className="bg-[#008069] text-white w-full select-none shadow-md z-30 sticky top-0">
      <div className="flex items-center h-[58px] px-1 sm:px-2">
        {/* Back Button */}
        <button
          onClick={onBackClick}
          aria-label="Back"
          className="p-2 -mr-1 rounded-full hover:bg-black/10 active:bg-black/20 transition-colors focus:outline-none flex items-center justify-center cursor-pointer"
          data-testid="back_button"
        >
          <ArrowLeft className="w-5 h-5 text-white" />
        </button>

        {/* Company Avatar */}
        <div className="w-[38px] h-[38px] rounded-full overflow-hidden bg-white/20 flex-shrink-0 cursor-pointer border border-white/20 shadow-xs mr-2.5">
          <img
            src="/company_logo.jpg"
            alt="Apex Customer Care Logo"
            className="w-full h-full object-cover"
            onError={(e) => {
              // Fallback to avatar with letters
              (e.target as HTMLElement).style.display = 'none';
            }}
          />
        </div>

        {/* Company Name & Online / Typing Status */}
        <div className="flex-1 min-w-0 flex flex-col justify-center cursor-pointer">
          <h1 className="text-[16px] font-semibold leading-tight text-white truncate">
            Apex Customer Care
          </h1>
          <span
            className={`text-[12px] leading-tight truncate transition-colors ${
              isTyping ? 'text-[#D9FDD3] font-medium' : 'text-white/85 font-normal'
            }`}
          >
            {isTyping ? 'typing...' : 'online'}
          </span>
        </div>

        {/* Video Call Button */}
        <button
          onClick={onVideoCallClick}
          aria-label="Video Call"
          className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-black/10 active:bg-black/20 transition-colors focus:outline-none cursor-pointer"
          data-testid="video_call_button"
        >
          <Video className="w-5 h-5 text-white" />
        </button>

        {/* Audio Call Button */}
        <button
          onClick={onAudioCallClick}
          aria-label="Audio Call"
          className="w-10 h-10 flex items-center justify-center rounded-full hover:bg-black/10 active:bg-black/20 transition-colors focus:outline-none cursor-pointer"
          data-testid="audio_call_button"
        >
          <Phone className="w-5 h-5 text-white" />
        </button>
      </div>
    </header>
  );
};
