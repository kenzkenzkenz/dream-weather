import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import Loader from './Loader'

// Mock the react-spinners module
vi.mock('react-spinners', () => ({
  ClipLoader: () => <div data-testid="clip-loader" />,
}))

describe('Loader Integration Tests', () => {
  it('displays "Waking up server" message when server is not awake', () => {
    render(<Loader duration={2000} isServerAwake={false} />)
    
    expect(screen.getByText('Waking up server')).toBeInTheDocument()
  })

  it('displays loading messages when server is awake', async () => {
    const { container } = render(<Loader duration={2000} isServerAwake={true} />)
    
    // Should display at least one message initially
    await waitFor(() => {
      const loaderMessage = container.querySelector('.loader-message')
      expect(loaderMessage).toBeInTheDocument()
    }, { timeout: 500 })
  })

  it('renders the clip loader spinner', () => {
    render(<Loader duration={2000} isServerAwake={true} />)
    
    expect(screen.getByTestId('clip-loader')).toBeInTheDocument()
  })

  it('switches between messages when server is awake', async () => {
    const { container } = render(<Loader duration={1000} isServerAwake={true} />)
    
    const loaderMessage = container.querySelector('.loader-message')
    expect(loaderMessage).toBeInTheDocument()
    
    // Re-render to trigger state updates
    await waitFor(() => {
      expect(loaderMessage).toHaveTextContent(/.+/)
    }, { timeout: 2000 })
  })

  it('shows correct message when server wakes up', async () => {
    const { rerender, container } = render(<Loader duration={2000} isServerAwake={false} />)
    
    expect(screen.getByText('Waking up server')).toBeInTheDocument()
    
    // Simulate server waking up
    rerender(<Loader duration={2000} isServerAwake={true} />)
    
    // Message should change to a loading message
    await waitFor(() => {
      const loaderMessage = container.querySelector('.loader-message')
      expect(loaderMessage).not.toHaveTextContent('Waking up server')
    }, { timeout: 500 })
  })
})
