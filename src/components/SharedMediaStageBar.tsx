import React from 'react';
import { X, Play, Music, FileText } from 'lucide-react';
import { SharedContentItem } from '../types/chat';

interface SharedMediaStageBarProps {
  items: SharedContentItem[];
  onRemoveItem: (item: SharedContentItem) => void;
  onClearAll: () => void;
}

export const SharedMediaStageBar: React.FC<SharedMediaStageBarProps> = ({
  items,
  onRemoveItem,
  onClearAll,
}) => {
  if (items.length === 0) return null;

  return (
    <div
      className="mx-2 mb-1 p-2 bg-[#F0F4F2] rounded-2xl shadow-md border border-gray-200 select-none z-20"
      data-testid="shared_media_stage_bar"
    >
      {/* Header */}
      <div className="flex items-center justify-between px-2 mb-2">
        <div className="flex items-center gap-1.5 text-xs">
          <span className="w-2 h-2 rounded-full bg-[#008069]" />
          <span className="font-bold text-[#111B21]">
            Shared Content ({items.length})
          </span>
          <span className="text-[#008069] font-medium">• Ready to send</span>
        </div>

        <button
          onClick={onClearAll}
          className="text-[#D32F2F] text-xs font-semibold hover:underline cursor-pointer"
          data-testid="shared_stage_cancel_all"
        >
          Cancel All
        </button>
      </div>

      {/* Horizontal Staged Items List */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1 px-1">
        {items.map((item) => (
          <div
            key={item.id}
            className="relative w-[100px] h-[82px] flex-shrink-0 bg-white rounded-xl border border-gray-200 overflow-hidden shadow-xs group"
          >
            {item.type === 'IMAGE' && (
              <img
                src={item.dataUrl}
                alt={item.fileName}
                className="w-full h-full object-cover"
              />
            )}

            {item.type === 'VIDEO' && (
              <div className="w-full h-full bg-[#1E2A30] relative flex items-center justify-center">
                {item.thumbnailUrl ? (
                  <img
                    src={item.thumbnailUrl}
                    alt={item.fileName}
                    className="w-full h-full object-cover"
                  />
                ) : null}
                <div className="absolute w-6 h-6 rounded-full bg-black/60 text-white flex items-center justify-center">
                  <Play className="w-3.5 h-3.5 fill-current ml-0.5" />
                </div>
              </div>
            )}

            {item.type === 'AUDIO' && (
              <div className="w-full h-full p-2 flex flex-col items-center justify-center text-center">
                <div className="w-8 h-8 rounded-full bg-[#E0663B]/15 text-[#E0663B] flex items-center justify-center mb-1">
                  <Music className="w-4 h-4" />
                </div>
                <div className="text-[10px] font-medium text-[#111B21] truncate w-full">
                  {item.fileName}
                </div>
                <div className="text-[9px] text-[#667781] truncate w-full">
                  {item.sizeOrDuration}
                </div>
              </div>
            )}

            {item.type === 'DOCUMENT' && (
              <div className="w-full h-full p-2 flex flex-col items-center justify-center text-center">
                <div className="w-8 h-8 rounded-full bg-[#7E57C2]/15 text-[#7E57C2] flex items-center justify-center mb-1">
                  <FileText className="w-4 h-4" />
                </div>
                <div className="text-[10px] font-medium text-[#111B21] truncate w-full">
                  {item.fileName}
                </div>
                <div className="text-[9px] text-[#667781] truncate w-full">
                  {item.sizeOrDuration}
                </div>
              </div>
            )}

            {/* Remove X Button */}
            <button
              onClick={() => onRemoveItem(item)}
              aria-label="Remove item"
              className="absolute top-1 right-1 w-5 h-5 rounded-full bg-[#37474F] border border-white text-white flex items-center justify-center hover:bg-black transition cursor-pointer"
              data-testid={`remove_shared_item_${item.id}`}
            >
              <X className="w-3 h-3" />
            </button>
          </div>
        ))}
      </div>
    </div>
  );
};
