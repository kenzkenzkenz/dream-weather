import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

async function init() {
  if (import.meta.env.DEV) {
    try {
      // Start MSW worker in development to intercept API calls locally
      const { worker } = await import('./mocks/browser')
      await worker.start({ onUnhandledRequest: 'bypass' })
    } catch (err) {
      // Don't block app rendering if the worker registration fails
      // Log to console for developer visibility
      // eslint-disable-next-line no-console
      console.error('MSW worker failed to start:', err)
    }
  }

  createRoot(document.getElementById('root')).render(
    <StrictMode>
      <App />
    </StrictMode>,
  )
}

init()
