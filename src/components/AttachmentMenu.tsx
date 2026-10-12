import React from 'react';
import { Image, Camera, FileText, Headphones } from 'lucide-react';

interface AttachmentMenuProps {
  isOpen: boolean;
  onDismiss: () => void;
  onPickGallery: () => void;
  onCaptureCamera: () => void;
  onPickDocuments: () => void;
  onPickAudio: () => void;
}

export const AttachmentMenu: React.FC<AttachmentMenuProps> = ({
  isOpen,
  onDismiss,
  onPickGallery,
  onCaptureCamera,
  onPickDocuments,
  onPickAudio,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/40 animate-fade-in">
      {/* Backdrop */}
      <div className="absolute inset-0" onClick={onDismiss} />

      {/* Sheet Content */}
      <div
        className="relative w-full max-w-lg bg-white rounded-t-3xl p-4 pb-8 shadow-2xl z-10 animate-slide-up"
        data-testid="attachment_menu_sheet"
      >
        {/* Drag Handle */}
        <div className="w-9 h-1 rounded-full bg-gray-300 mx-auto mb-5" />

        {/* Exactly 4 options: Gallery, Camera, Documents, Audio */}
        <div className="grid grid-cols-4 gap-2 text-center">
          {/* Gallery */}
          <AttachmentOption
            label="Gallery"
            icon={<Image className="w-6 h-6 text-[#1E88E5]" />}
            onClick={() => {
              onDismiss();
              onPickGallery();
            }}
          />

          {/* Camera */}
          <AttachmentOption
            label="Camera"
            icon={<Camera className="w-6 h-6 text-[#E91E63]" />}
            onClick={() => {
              onDismiss();
              onCaptureCamera();
            }}
          />

          {/* Documents */}
          <AttachmentOption
            label="Documents"
            icon={<FileText className="w-6 h-6 text-[#7E57C2]" />}
            onClick={() => {
              onDismiss();
              onPickDocuments();
            }}
          />

          {/* Audio */}
          <AttachmentOption
            label="Audio"
            icon={<Headphones className="w-6 h-6 text-[#E0663B]" />}
            onClick={() => {
              onDismiss();
              onPickAudio();
            }}
          />
        </div>
      </div>
    </div>
  );
};

const AttachmentOption: React.FC<{
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
}> = ({ label, icon, onClick }) => {
  return (
    <button
      onClick={onClick}
      className="flex flex-col items-center gap-1.5 p-2 rounded-xl hover:bg-gray-50 active:scale-95 transition cursor-pointer group"
    >
      <div className="w-14 h-14 rounded-2xl bg-[#F8F9FA] border border-[#E4E6EB] flex items-center justify-center group-hover:border-gray-300 shadow-xs">
        {icon}
      </div>
      <span className="text-[12.5px] font-medium text-[#111B21]">{label}</span>
    </button>
  );
};
