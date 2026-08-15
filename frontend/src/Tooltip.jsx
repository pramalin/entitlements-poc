import { useState } from "react";

export default function Tooltip({ text, riskNote, children }) {
  const [pos, setPos] = useState(null);

  const show = (e) => setPos({ x: e.clientX, y: e.clientY });
  const move = (e) => setPos({ x: e.clientX, y: e.clientY });
  const hide = () => setPos(null);

  return (
    <>
      <span onMouseEnter={show} onMouseMove={move} onMouseLeave={hide}>
        {children}
      </span>
      {pos && (
        <div
          className="tooltip visible"
          style={{ left: pos.x + 14, top: pos.y + 14 }}
        >
          {text ? (
            <>
              {text}
              {riskNote && <span className="risk">⚠ {riskNote}</span>}
            </>
          ) : (
            <span className="missing">
              No description generated yet for this entitlement.
            </span>
          )}
        </div>
      )}
    </>
  );
}
