# Customer Support Chat

WhatsApp-style customer chat conversation interface with live messaging, authentic green header, message bubbles, and bottom composer. Ported from Android (Jetpack Compose) to React + TypeScript with Tailwind CSS and Vite.

## Features

- **WhatsApp Green Top Bar**: Official `#008069` header with Apex Customer Care logo, online/typing status indicator, and Audio/Video call buttons.
- **Doodle Wallpaper & Layout**: Authentic WhatsApp doodle wallpaper canvas with chat bubbles, end-to-end encryption notice banner, and "Today" date badge.
- **Message Bubbles**:
  - Outgoing text & media bubbles (`#D9FDD3`) with read status double checkmarks.
  - Outgoing voice note bubble with WhatsApp pink accent (`#E91E63`), circular sender avatar with mic badge, interactive 30-bar waveform track, and scrub/seek controls.
  - Document attachments (`#7F66FF`) and forwarded audio tracks (`#E0663B`).
  - Image and Video message bubbles with full-screen lightbox previews.
- **Interactive Voice Notes & Message Composer**:
  - Normal idle mode with text input, emoji picker toggle, attachment sheet trigger, camera capture, and mic button.
  - WhatsApp locked hands-free recording mode with live microphone audio waveform, timer, View Once `(1)` indicator, Delete trash button, `[ Pause / Resume ]` elongated pill, and circular Send button.
  - Press-and-hold slide-to-lock / slide-to-cancel gestures.
- **Attachment Menu & Pickers**:
  - 4-item bottom sheet: Gallery, Camera, Documents, Audio.
  - Device Audio Picker with instant search, folder browsing, audio playback preview, and direct dispatch.
  - Pre-send Media Preview dialog with caption input for photos and video clips.
- **Calling Interface**:
  - Full-screen Audio Call with ringing simulation, call timer, and mute/speaker controls.
  - Full-screen Video Call with local camera picture-in-picture preview, camera switch, and screen sharing controls.
- **Integrated Emoji Drawer**:
  - Categorized emoji tray with Smileys, Hands, Hearts, and Objects tabs + Backspace action.
- **Scroll to Bottom FAB**:
  - Floating double-arrow down button that appears when scrolling up through message history.

## Getting Started

```bash
npm install
npm run dev
```

Server runs on port 3000 at `http://localhost:3000`.
