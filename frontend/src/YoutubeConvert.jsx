import React from "react";

export default function YoutubeConvert(url) {
  if (!url) return null;

  try {
    const parsed = new URL(url);

    // youtube.com/watch?v=VIDEOID
    if (parsed.hostname.includes("youtube.com")) {
      const videoId = parsed.searchParams.get("v");
      if (videoId) return `https://www.youtube.com/embed/${videoId}`;
    }

    // youtu.be/VIDEOID
    if (parsed.hostname === "youtu.be") {
      const videoId = parsed.pathname.slice(1);
      if (videoId) return `https://www.youtube.com/embed/${videoId}`;
    }

    return null;
  } catch {
    return null;
  }
}