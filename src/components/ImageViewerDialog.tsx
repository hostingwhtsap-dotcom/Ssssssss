import React from 'react';
import { ArrowLeft } from 'lucide-react';

interface ImageViewerDialogProps {
  imagePath: string | null;
  onDismiss: () => void;
}

export const ImageViewerDialog: React.FC<ImageViewerDialogProps> = ({
  imagePath,
  onDismiss,
}) => {
  if (!imagePath) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black flex flex-col justify-between animate-fade-in select-none">
      {/* Top Bar */}
      <div className="p-3 flex items-center z-10 bg-gradient-to-b from-black/70 to-transparent">
        <button
          onClick={onDismiss}
          className="p-2 rounded-full hover:bg-white/10 active:scale-95 text-white transition cursor-pointer"
        >
          <ArrowLeft className="w-6 h-6" />
        </button>
      </div>

      {/* Center Image */}
      <div className="flex-1 flex items-center justify-center p-2 overflow-hidden">
        <img
          src={imagePath}
          alt="Full screen photo"
          className="max-h-full max-w-full object-contain"
        />
      </div>
    </div>
  );
};
