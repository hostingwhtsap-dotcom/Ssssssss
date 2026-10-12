import React, { useState } from 'react';
import { Delete } from 'lucide-react';

interface EmojiPickerProps {
  onEmojiSelected: (emoji: string) => void;
  onBackspaceClick: () => void;
}

const CATEGORIES = [
  {
    name: 'Smileys',
    emojis: [
      '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
      '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
      '😋', '😛', '😜', '🤪', '😝', '🤗', '🤭', '🤫', '🤔', '🤐',
      '🤨', '😐', '😑', '😶', '😏', '😒', '🙄', '😬', '😮', '😴',
    ],
  },
  {
    name: 'Hands',
    emojis: [
      '👍', '👎', '👏', '🙌', '👐', '🤲', '🤝', '🙏', '✌️', '🤞',
      '🤟', '🤘', '🤙', '👈', '👉', '👆', '👇', '☝️', '👋', '🤚',
      '🖐️', '✋', '🖖', '✍️', '🤳', '💪',
    ],
  },
  {
    name: 'Hearts',
    emojis: [
      '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
      '❣️', '💕', '💞', '💓', '💗', '💖', '💘', '💝', '💟', '✨',
      '🔥', '🎉', '⭐', '🌟',
    ],
  },
  {
    name: 'Objects',
    emojis: [
      '📦', '📱', '💻', '⌨️', '🖥️', '🖨️', '📞', '📟', '📠', '🔋',
      '🔌', '💡', '🔦', '🕯️', '📄', '📁', '📂', '💼', '✉️', '📧',
      '📩', '📨', '🏷️', '📌',
    ],
  },
];

export const EmojiPickerView: React.FC<EmojiPickerProps> = ({
  onEmojiSelected,
  onBackspaceClick,
}) => {
  const [selectedCategory, setSelectedCategory] = useState(0);

  return (
    <div
      className="w-full bg-[#F7F7F8] rounded-t-2xl shadow-lg border-t border-gray-200 select-none z-20 flex flex-col h-[240px]"
      data-testid="emoji_picker_tray"
    >
      {/* Category Tabs Header */}
      <div className="flex items-center justify-between px-2 py-1.5 bg-[#EDEDED] border-b border-gray-200">
        <div className="flex items-center gap-1 overflow-x-auto">
          {CATEGORIES.map((cat, idx) => (
            <button
              key={cat.name}
              onClick={() => setSelectedCategory(idx)}
              className={`px-3 py-1 rounded-md text-[12.5px] font-medium transition cursor-pointer ${
                selectedCategory === idx
                  ? 'text-[#008069] font-bold bg-white/70 shadow-2xs'
                  : 'text-[#111B21] hover:text-[#008069]'
              }`}
            >
              {cat.name}
            </button>
          ))}
        </div>

        {/* Backspace Action */}
        <button
          onClick={onBackspaceClick}
          aria-label="Backspace"
          className="p-1.5 rounded-full hover:bg-black/10 text-[#54656F] transition cursor-pointer"
        >
          <Delete className="w-5 h-5" />
        </button>
      </div>

      {/* Emoji Grid */}
      <div className="flex-1 overflow-y-auto p-2 grid grid-cols-8 gap-1.5 text-center items-center">
        {CATEGORIES[selectedCategory].emojis.map((emoji, index) => (
          <button
            key={index}
            onClick={() => onEmojiSelected(emoji)}
            className="w-10 h-10 flex items-center justify-center text-2xl hover:bg-gray-200/60 rounded-lg active:scale-90 transition cursor-pointer"
          >
            {emoji}
          </button>
        ))}
      </div>
    </div>
  );
};
