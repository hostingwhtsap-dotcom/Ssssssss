import { ChatMessage } from '../types/chat';
import { generateWaveform } from './audioHelper';

const STORAGE_KEY = 'chat_messages_store_v1';

export function getFormattedCurrentTime(): string {
  const now = new Date();
  let hours = now.getHours();
  const minutes = now.getMinutes();
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12;
  hours = hours ? hours : 12;
  const minutesStr = minutes < 10 ? '0' + minutes : minutes;
  return `${hours}:${minutesStr} ${ampm}`;
}

export function formatFileSize(bytes: number): string {
  if (bytes >= 1024 * 1024) {
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }
  if (bytes >= 1024) {
    return Math.round(bytes / 1024) + ' KB';
  }
  return bytes + ' B';
}

export function formatDuration(seconds: number): string {
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
}

export function loadStoredMessages(): ChatMessage[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed;
      }
    }
  } catch (e) {
    console.warn('Failed to read messages from localStorage', e);
  }

  // Initial seed messages reflecting the authentic customer care conversation (Screenshot 3)
  const seedWaveform = generateWaveform('seed_voice_1', 30);
  const initialMessages: ChatMessage[] = [
    {
      id: 'msg_seed_1',
      text: 'Hello! Welcome to Apex Customer Care. How can our support team assist you today?',
      timestamp: '10:14 AM',
      isOutgoing: false,
      status: 'READ',
      attachmentType: 'NONE',
      waveform: [],
    },
    {
      id: 'msg_seed_2',
      text: 'Hi, I need help verifying my shipment and sending the receipt photo.',
      timestamp: '10:15 AM',
      isOutgoing: true,
      status: 'READ',
      attachmentType: 'NONE',
      waveform: [],
    },
    {
      id: 'msg_seed_3',
      text: '',
      timestamp: '10:16 AM',
      isOutgoing: true,
      status: 'READ',
      attachmentType: 'VOICE_NOTE',
      mediaPath: null,
      audioDuration: '0:03',
      waveform: seedWaveform,
    },
    {
      id: 'msg_seed_4',
      text: 'We have received your voice note and our support specialists are on standby to help!',
      timestamp: '10:16 AM',
      isOutgoing: false,
      status: 'READ',
      attachmentType: 'NONE',
      waveform: [],
    },
  ];

  saveStoredMessages(initialMessages);
  return initialMessages;
}

export function saveStoredMessages(messages: ChatMessage[]) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
  } catch (e) {
    console.warn('Failed to persist messages to localStorage', e);
  }
}
