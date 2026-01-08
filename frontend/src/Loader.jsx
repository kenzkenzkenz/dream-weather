import React from "react";
import { ClipLoader } from "react-spinners";
import { useEffect, useState } from "react";
import { getMessages } from './Utils';

export default function Loader({ duration = 4000, isServerAwake }) {
  const [index, setIndex] = useState(0);
  const messages = getMessages(isServerAwake);

  const stepTime = duration / (messages.length - 1);

  useEffect(() => {
    const interval = setInterval(() => {
      setIndex((prev) => {
        if (prev < messages.length - 1) {
          return prev + 1;
        }
        return prev;
      });
    }, stepTime);
    return () => clearInterval(interval);
  }, [stepTime, messages.length]);

  const safeIndex = Math.min(index, messages.length - 1);

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
      <p className="loader-message">{messages[safeIndex]}</p>
    </div>
  );
}