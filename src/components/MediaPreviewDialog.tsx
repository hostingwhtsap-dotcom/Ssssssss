import React, { useState } from 'react';
import { ArrowLeft, Send, Play, Video } from 'lucide-react';
import { PendingMediaPreview } from '../types/chat';

interface MediaPreviewDialogProps {
  preview: PendingMediaPreview | null;
  onSend: (caption: string) => void;
  onCancel: () => void;
}

export const MediaPreviewDialog: React.FC<MediaPreviewDialogProps> = ({
  preview,
  onSend,
  onCancel,
}) => {
  const [caption, setCaption] = useState('');

  if (!preview) return null;

  const handleSend = () => {
    onSend(caption.trim());
    setCaption('');
  };

  return (
    <div
      className="fixed inset-0 z-50 bg-black flex flex-col justify-between animate-fade-in"
      data-testid="media_preview_dialog"
    >
      {/* Top Bar */}
      <div className="flex items-center gap-2 p-3 text-white z-10 bg-gradient-to-b from-black/60 to-transparent">
        <button
          onClick={onCancel}
          className="p-2 rounded-full hover:bg-white/10 active:scale-95 transition cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5 text-white" />
        </button>
        <span className="text-[18px] font-semibold">
          {preview.isVideo ? 'Video Preview' : 'Photo Preview'}
        </span>
      </div>

      {/* Media Content */}
      <div className="flex-1 relative flex items-center justify-center p-4 overflow-hidden">
        {preview.isVideo ? (
          <div className="relative max-h-full max-w-full flex items-center justify-center">
            {preview.thumbnailUrl ? (
              <img
                src={preview.thumbnailUrl}
                alt="Video Preview"
                className="max-h-[70vh] max-w-full rounded-lg object-contain shadow-2xl"
              />
            ) : (
              <div className="w-72 h-80 bg-neutral-900 rounded-lg flex items-center justify-center text-white/50">
                <Video className="w-16 h-16" />
              </div>
            )}
            {/* Play Button Overlay */}
            <div className="absolute w-16 h-16 rounded-full bg-black/60 text-white flex items-center justify-center shadow-lg">
              <Play className="w-8 h-8 fill-current ml-1" />
            </div>
            {/* Duration Chip */}
            {preview.duration && (
              <div className="absolute bottom-3 left-3 bg-black/75 text-white text-[12px] font-medium px-2.5 py-1 rounded-full flex items-center gap-1.5">
                <Video className="w-3.5 h-3.5" />
                <span>{preview.duration}</span>
              </div>
            )}
          </div>
        ) : (
          <img
            src={preview.fileOrUrl}
            alt="Photo Preview"
            className="max-h-[72vh] max-w-full rounded-lg object-contain shadow-2xl"
          />
        )}
      </div>

      {/* Bottom Caption & Send Bar */}
      <div className="p-3 bg-gradient-to-t from-black via-black/80 to-transparent flex items-center gap-2.5 z-10">
        <div className="flex-1 h-12 bg-[#1F2C34] rounded-full px-4 flex items-center">
          <input
            type="text"
            value={caption}
            onChange={(e) => setCaption(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSend()}
            placeholder="Add a caption..."
            className="w-full text-white text-[15px] placeholder-[#8696A0] bg-transparent focus:outline-none"
            data-testid="caption_input_field"
            autoFocus
          />
        </div>

        {/* Send Button */}
        <button
          onClick={handleSend}
          className="w-12 h-12 rounded-full bg-[#E91E63] hover:bg-[#D81B60] active:scale-95 transition flex items-center justify-center text-white shadow-lg cursor-pointer flex-shrink-0"
          data-testid="send_media_button"
        >
          <Send className="w-5 h-5 ml-0.5" />
        </button>
      </div>
    </div>
  );
};
