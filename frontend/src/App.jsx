import { useState, useEffect, useRef } from 'react'
import './App.css'
import Match from './Match';
import Form from './Form';
import Loader from './Loader';
import TryAgainButton from './TryAgainButton';
import { PuffLoader, DotLoader } from "react-spinners";

function App() {
  const [match, setMatch] = useState(null);
  const [status, setStatus] = useState('idle'); // 'idle' | 'loading' | 'error' | 'success' | 'no-data' | 'rate-limit'
  const [isServerAwake, setIsServerAwake] = useState(false);
  const [reportThanks, setReportThanks] = useState("");
  const [reportLoader, setReportLoader] = useState(false);
  const hasCalledWakeUp = useRef(false);
  const backendUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
  const headers = {
    'Content-Type': 'application/json',
  };

  useEffect(() => {
    if (!hasCalledWakeUp.current) {
      wakeUp();
      hasCalledWakeUp.current = true;
    }
  }, []);

  const wakeUp = async () => {
    try {
      const res = await fetch(`${backendUrl}/api/v1/wakeup`, {
        method: 'GET',
        headers: headers,
      });

      if (!res.ok) {
        setIsServerAwake(false);
        setStatus('error');
      } else {
        setIsServerAwake(true);
      }
    } catch (error) {
      setIsServerAwake(false);
      setStatus('error');
    }
  }

  const handleSubmit = async (formData) => {
    setStatus('loading');
    try {
      const response = await fetch(`${backendUrl}/api/v1/livestream/match`, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(formData)
      });

      if (!response.ok) {
        if (response.status === 404) {
          setStatus('no-data');
        }
        else if (response.status === 429) {
          setStatus('rate-limit');
        }
        else {
          setStatus('error');
        }
        return;
      }

      const data = await response.json();
      if (!data) {
        setStatus('no-data');
        return;
      }

      setMatch(data);
      setStatus('success');
    } catch (error) {
      setStatus('error');
    }
  };

  const sendReport = async (data) => {
    try {
      const response = await fetch(`${backendUrl}/api/v1/livestream/report`, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(data)
      });

      setReportLoader(false);

      if (!response.ok) {
        setStatus('error');
        return;
      }

      setReportThanks("Thanks for the report!");
    } catch (error) {
      setStatus('error');
    } finally {
      setReportLoader(false);
    }
  };

  const handleReport = (e) => {
    e.preventDefault();
    if (reportLoader) return;
    setReportLoader(true);

    let req = {
      slug: match?.data?.slug,
      stream_url: match?.data?.stream_url,
    };
    sendReport(req);
  }

  return (
    <div className='app-container'>
      {!isServerAwake && status === 'idle' &&
        <div className="server-loader-container">
          <PuffLoader size={10} color={'#3498db'} />
          <p className="pulse-text">Waking up server...</p>
        </div>
      }
      {status === 'idle' && <Form onSubmit={handleSubmit} />}
      {status === 'loading' && <Loader duration={8000} isServerAwake={isServerAwake} />}
      {status === 'error' && (
        <div>
          <p>There was an error. Please try again.</p>
          <TryAgainButton onClick={() => setStatus('idle')} />
        </div>
      )}
      {status === 'no-data' && (
        <div>
          <p>No matching locations found. Try again later.</p>
          <TryAgainButton onClick={() => setStatus('idle')} />
        </div>
      )}
      {status === "rate-limit" && (
        <div>
          <p>Too many requests! Maybe take a break and go outside.</p>
          <TryAgainButton onClick={() => setStatus("idle")} />
        </div>
      )}

      {status === 'success' &&
        (
          <div>
            <Match match={match} />
            <div>

              <div>
                <TryAgainButton
                  onClick={() => {
                    setStatus('idle');
                    setMatch(null);
                    setReportThanks("");
                  }} />
              </div>

              <br /><br />

              {!reportThanks && !reportLoader && (
                <label style={{ marginRight: "30px", fontSize: "14px" }}>
                  <span>Livestream unavailable for this location? </span>
                  <input
                    type="button"
                    id="report"
                    name="report"
                    value="Report"
                    disabled={reportLoader}
                    className="report-button"
                    onClick={handleReport}
                  />
                </label>
              )}
              {reportLoader && (
                <div className="report-loader-container">
                  <DotLoader size={10} color={'#3498db'} />
                  <p>Sending report...</p>
                </div>
              )}
              {reportThanks && !reportLoader && (
                <div className='report-class'>
                  {reportThanks}
                </div>
              )
              }
            </div>
          </div>
        )
      }

      <footer>
        Webcam data provided by{' '}
        <a href="https://openwebcamdb.com" target="_blank" rel="noopener noreferrer">OpenWebcamDB.</a>
        {"  "}
        <br />
        Weather data provided by{' '}
        <a href="https://www.weather.gov" target="_blank" rel="noopener noreferrer">NOAA / NWS.</a>
        <br />
        App developed by{' '}
        <a href="https://github.com/kenzkenzkenz" target="_blank" rel="noopener noreferrer">Mackenzie Allen.</a>
        {"  "}
        <br />
        <a href="https://github.com/kenzkenzkenz/dream-weather" target="_blank" rel="noopener noreferrer">
          View source code on GitHub.
        </a>
        <br />
        Dream Weather <em>(US Edition)</em>
      </footer>

    </div>
  )
}

export default App
