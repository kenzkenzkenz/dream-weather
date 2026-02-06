import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import App from './App'

// Mock the react-spinners module
vi.mock('react-spinners', () => ({
  PuffLoader: () => <div data-testid="puff-loader" />,
  DotLoader: () => <div data-testid="dot-loader" />,
  ClipLoader: () => <div data-testid="clip-loader" />,
}))

describe('App Integration Tests', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renders the Title component when app mounts', async () => {
    render(<App />)
    
    await waitFor(() => {
      expect(screen.getByText('Dream Weather')).toBeInTheDocument()
    })
  })
})
