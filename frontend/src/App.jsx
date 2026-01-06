import { useState } from 'react'
import './App.css'
import Match from './Match';
import Form from './Form';
import Loader from './Loader';
import TryAgainButton from './TryAgainButton';

function App() {
  const [match, setMatch] = useState(null);
  const [status, setStatus] = useState('idle'); // 'idle' | 'loading' | 'error' | 'success' | 'no-data' | 'rate-limit'

  const handleSubmit = async (formData) => {
    setStatus('loading');
    try {
      const response = await fetch('http://192.168.1.134:8080/api/v1/livestream/match', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
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

  return (
    <div>
      {status === 'idle' && <Form onSubmit={handleSubmit} />}
      {status === 'loading' && <Loader duration={8000} />}
      {status === 'error' && (
        <div>
          <p>There was an error fetching your match. Please try again.</p>
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

      {status === 'success' && (
        <div>
          <Match match={match} />
          <TryAgainButton
            onClick={() => {
              setStatus('idle');
              setMatch(null);
            }}
          />
        </div>
      )}

      <footer style={{ fontSize: '0.75rem', textAlign: 'center', marginTop: '2rem' }}>
        Webcam data provided by{' '}
        <a href="https://openwebcamdb.com" target="_blank" rel="noopener noreferrer">OpenWebcamDB.</a>
        {"  "}
        Weather data provided by{' '}
        <a href="https://www.weather.gov" target="_blank" rel="noopener noreferrer">NOAA / NWS.</a>
        <br />
        App developed by{' '}
        <a href="https://github.com/kenzkenzkenz" target="_blank" rel="noopener noreferrer">Mackenzie Allen.</a>
        {"  "}
        <a href="https://github.com/kenzkenzkenz/dream-weather" target="_blank" rel="noopener noreferrer">
          View source code on GitHub.
        </a>
        <br />
        <em>(US Edition)</em>
      </footer>

    </div>
  )
}

export default App
