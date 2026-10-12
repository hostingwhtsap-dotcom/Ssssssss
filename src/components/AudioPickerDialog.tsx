import React, { useState, useRef } from 'react';
import {
  ArrowLeft,
  Send,
  Search,
  X,
  FolderOpen,
  Play,
  Pause,
  Music,
} from 'lucide-react';
import { DeviceAudioItem } from '../types/chat';

interface AudioPickerDialogProps {
  isOpen: boolean;
  onDismiss: () => void;
  onSelectAudio: (file: File | null, fileName: string, sizeOrDuration: string, dataUrl: string) => void;
}

const DEFAULT_AUDIO_ITEMS: DeviceAudioItem[] = [
  {
    id: 'audio_1',
    displayName: 'Order_Inquiry_VoiceRecord.mp3',
    title: 'Order Inquiry Voice Record',
    artist: 'Customer Support',
    durationFormatted: '0:42',
    sizeFormatted: '640 KB',
  },
  {
    id: 'audio_2',
    displayName: 'Apex_Customer_Helpline_Prompt.m4a',
    title: 'Customer Helpline Prompt',
    artist: 'Apex Voice Bot',
    durationFormatted: '1:15',
    sizeFormatted: '1.2 MB',
  },
  {
    id: 'audio_3',
    displayName: 'Product_Troubleshooting_Guide.wav',
    title: 'Product Troubleshooting Guide',
    artist: 'Support Specialist',
    durationFormatted: '2:04',
    sizeFormatted: '3.4 MB',
  },
  {
    id: 'audio_4',
    displayName: 'Warranty_Verification_Note.aac',
    title: 'Warranty Verification Note',
    artist: 'Quality Assurance',
    durationFormatted: '0:28',
    sizeFormatted: '480 KB',
  },
];

export const AudioPickerDialog: React.FC<AudioPickerDialogProps> = ({
  isOpen,
  onDismiss,
  onSelectAudio,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedItem, setSelectedItem] = useState<DeviceAudioItem | null>(null);
  const [previewingId, setPreviewingId] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  if (!isOpen) return null;

  const filteredItems = DEFAULT_AUDIO_ITEMS.filter((item) => {
    const q = searchQuery.toLowerCase();
    return (
      item.displayName.toLowerCase().includes(q) ||
      item.title.toLowerCase().includes(q) ||
      item.artist.toLowerCase().includes(q)
    );
  });

  const handleSystemFilePicked = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const dataUrl = URL.createObjectURL(file);
      const sizeStr = file.size > 1024 * 1024
        ? `${(file.size / (1024 * 1024)).toFixed(1)} MB`
        : `${Math.round(file.size / 1024)} KB`;
      onSelectAudio(file, file.name, sizeStr, dataUrl);
      onDismiss();
    }
  };

  const handleSendSelected = (item: DeviceAudioItem) => {
    onSelectAudio(null, item.displayName, `${item.durationFormatted} • ${item.sizeFormatted}`, item.dataUrl || '');
    onDismiss();
  };

  return (
    <div
      className="fixed inset-0 z-50 flex flex-col bg-[#F7F8FA] animate-fade-in"
      data-testid="audio_picker_dialog"
    >
      {/* Top Header */}
      <div className="bg-[#008069] text-white flex items-center px-2 py-3 shadow-sm">
        <button
          onClick={onDismiss}
          className="p-2 rounded-full hover:bg-black/10 active:scale-95 transition cursor-pointer"
          data-testid="audio_picker_back_button"
        >
          <ArrowLeft className="w-5 h-5 text-white" />
        </button>

        <div className="flex-1 ml-2">
          <h2 className="text-[19px] font-bold leading-tight">Send audio</h2>
          <p className="text-[12px] text-white/85">
            {filteredItems.length} audio files found
          </p>
        </div>

        {selectedItem && (
          <button
            onClick={() => handleSendSelected(selectedItem)}
            className="p-2 rounded-full hover:bg-black/10 active:scale-95 transition cursor-pointer"
            data-testid="audio_picker_send_button"
          >
            <Send className="w-5 h-5 text-white" />
          </button>
        )}
      </div>

      {/* Search Bar & System Picker Action */}
      <div className="bg-white p-3 shadow-xs space-y-2.5">
        <div className="relative flex items-center">
          <Search className="w-4 h-4 text-[#8696A0] absolute left-3 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search songs, artists, audio files..."
            className="w-full bg-[#F7F8FA] border border-gray-200 rounded-full pl-9 pr-9 py-2 text-[14px] text-[#111B21] focus:outline-none focus:border-[#008069]"
            data-testid="audio_search_input"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-3 text-[#8696A0] hover:text-[#111B21] cursor-pointer"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Native File Picker Trigger Card */}
        <div
          onClick={() => fileInputRef.current?.click()}
          className="flex items-center gap-3 p-3 bg-[#E8F5E9] hover:bg-[#DDF2DF] rounded-xl cursor-pointer border border-[#C8E6C9] transition active:scale-99"
          data-testid="browse_system_audio_files_button"
        >
          <div className="w-9 h-9 rounded-full bg-[#008069] flex items-center justify-center text-white flex-shrink-0">
            <FolderOpen className="w-5 h-5" />
          </div>
          <div className="flex-1 min-w-0">
            <h4 className="text-[14px] font-semibold text-[#008069]">
              Browse device storage / Files
            </h4>
            <p className="text-[11.5px] text-[#667781] truncate">
              Choose MP3, M4A, AAC, WAV, OGG, FLAC directly
            </p>
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept="audio/*,.mp3,.m4a,.aac,.wav,.ogg,.flac"
            className="hidden"
            onChange={handleSystemFilePicked}
          />
        </div>
      </div>

      {/* Audio List */}
      <div className="flex-1 overflow-y-auto divide-y divide-gray-100">
        {filteredItems.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-8 text-center text-[#667781]">
            <Music className="w-12 h-12 text-gray-300 mb-3" />
            <p className="text-[15px] font-medium text-[#111B21]">No matching audio files</p>
            <p className="text-[12px] mt-1 max-w-xs">
              Tap &quot;Browse device storage / Files&quot; above to pick any audio file from your device.
            </p>
          </div>
        ) : (
          filteredItems.map((item) => {
            const isSelected = selectedItem?.id === item.id;
            const isPreviewing = previewingId === item.id;

            return (
              <div
                key={item.id}
                onClick={() => setSelectedItem(isSelected ? null : item)}
                className={`flex items-center gap-3 px-4 py-3 transition cursor-pointer ${
                  isSelected ? 'bg-[#E8F5E9]' : 'bg-white hover:bg-gray-50'
                }`}
              >
                {/* Play Preview Button */}
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    setPreviewingId(isPreviewing ? null : item.id);
                  }}
                  className={`w-10 h-10 rounded-full flex items-center justify-center transition flex-shrink-0 ${
                    isPreviewing
                      ? 'bg-[#008069] text-white'
                      : 'bg-[#E0663B]/15 text-[#E0663B] hover:bg-[#E0663B]/25'
                  }`}
                >
                  {isPreviewing ? (
                    <Pause className="w-5 h-5 fill-current" />
                  ) : (
                    <Play className="w-5 h-5 fill-current ml-0.5" />
                  )}
                </button>

                {/* Details */}
                <div className="flex-1 min-w-0">
                  <div className="text-[14.5px] font-semibold text-[#111B21] truncate">
                    {item.displayName}
                  </div>
                  <div className="text-[12px] text-[#667781] truncate">
                    {item.artist} • {item.durationFormatted} • {item.sizeFormatted}
                  </div>
                </div>

                {/* Direct Send Icon */}
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleSendSelected(item);
                  }}
                  className="p-2 text-[#9E9E9E] hover:text-[#008069] transition cursor-pointer"
                >
                  <Send className="w-4 h-4" />
                </button>
              </div>
            );
          })
        )}
      </div>

      {/* Bottom Selected Confirmation Bar */}
      {selectedItem && (
        <div className="bg-white p-3 border-t border-gray-200 flex items-center justify-between shadow-lg">
          <div className="min-w-0 flex-1 mr-3">
            <div className="text-[14px] font-bold text-[#111B21] truncate">
              {selectedItem.displayName}
            </div>
            <div className="text-[12px] text-[#667781]">
              {selectedItem.durationFormatted} • {selectedItem.sizeFormatted}
            </div>
          </div>

          <button
            onClick={() => handleSendSelected(selectedItem)}
            className="bg-[#008069] hover:bg-[#00735E] active:scale-95 text-white font-medium text-[14px] px-5 py-2.5 rounded-full flex items-center gap-1.5 shadow-md cursor-pointer flex-shrink-0"
            data-testid="confirm_send_audio_button"
          >
            <Send className="w-4 h-4" />
            <span>Send Audio</span>
          </button>
        </div>
      )}
    </div>
  );
};
