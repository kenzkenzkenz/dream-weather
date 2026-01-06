import React from 'react';
import YoutubeConvert from './YoutubeConvert';

export default function Match({ match }) {
  // Determine if YouTube or iframe stream
  const isYoutube = match?.data?.stream_type?.toLowerCase() === 'youtube';
  const embedUrl = isYoutube
    ? YoutubeConvert(match?.data?.stream_url)
    : match?.data?.stream_url;

  // If no valid stream, show fallback
  if (!embedUrl) {
    return (
      <div className="match-container">
        <h1>Dream Weather</h1>
        <h2>{match?.data?.title}, {match?.data?.country?.name}</h2>
        <p>Current Conditions: {match?.data?.forecast?.shortForecast}, {match?.data?.forecast?.temperature}&deg; F</p>
        <p>{match?.data?.description}</p>
        <p>Livestream unavailable.</p>
        {match?.data?.stream_url && (
          <a href={match.data.stream_url} target="_blank" rel="noopener noreferrer">
            Try viewing on the original site
          </a>
        )}
      </div>
    );
  }

  return (
    <div className="match-container">
      <h1>Dream Weather</h1>
      <h2>{match?.data?.title}, {match?.data?.country?.name}</h2>
      <p>Current Conditions: {match?.data?.forecast?.shortForecast}, {match?.data?.forecast?.temperature}&deg; F</p>
      <p>{match?.data?.description}</p>
      <div className="iframe-container">
        <iframe
          src={embedUrl}
          title={match?.data?.title}
          frameBorder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          referrerPolicy="strict-origin-when-cross-origin"
          allowFullScreen
        />
      </div>
    </div>
  );
}