import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import TryAgainButton from './TryAgainButton'

describe('TryAgainButton', () => {
  it('renders the button with correct text', () => {
    render(<TryAgainButton onClick={() => {}} />)
    const button = screen.getByRole('button', { name: /go again/i })
    expect(button).toBeInTheDocument()
  })

  it('calls onClick handler when clicked', async () => {
    const handleClick = vi.fn()
    const user = userEvent.setup()
    render(<TryAgainButton onClick={handleClick} />)
    
    const button = screen.getByRole('button', { name: /go again/i })
    await user.click(button)
    
    expect(handleClick).toHaveBeenCalledOnce()
  })

  it('calls onClick handler multiple times when clicked multiple times', async () => {
    const handleClick = vi.fn()
    const user = userEvent.setup()
    render(<TryAgainButton onClick={handleClick} />)
    
    const button = screen.getByRole('button', { name: /go again/i })
    await user.click(button)
    await user.click(button)
    
    expect(handleClick).toHaveBeenCalledTimes(2)
  })
})
