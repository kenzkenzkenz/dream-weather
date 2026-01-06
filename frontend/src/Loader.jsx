import React from "react";
import { ClipLoader } from "react-spinners";
import { useEffect, useState } from "react";

const messages = [
  "Fetching locations…",
  "Checking the forecast…",
  "Finding the perfect webcam…",
  "Almost there…"
];

export default function Loader({ duration = 4000 }) {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    const stepTime = duration / messages.length - 1;

    const interval = setInterval(() => {
      setIndex((prev) => {
        if (prev < messages.length - 1) {
          return prev + 1;
        }
        return prev;
      });
    }, stepTime);

    return () => clearInterval(interval);
  }, [duration]);

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        padding: "2rem"
      }}
    >
      <ClipLoader color="#3498db" size={45} />
      <p className="loader-message">{messages[index]}</p>
    </div>
  );
}