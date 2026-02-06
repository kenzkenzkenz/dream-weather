import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Form from './Form'

describe('Form component tests', () => {
  it('renders the form and title', () => {
    const { container } = render(<Form onSubmit={() => {}} />)
    const form = container.querySelector('form')
    expect(form).toBeInTheDocument()
    expect(screen.getByText(/Tell us what vibe/i)).toBeInTheDocument()
  })

  it('disables submit with invalid weather combinations', async () => {
    const user = userEvent.setup()
    render(<Form onSubmit={() => {}} />)

    const snowRadio = screen.getByRole('radio', { name: /snow/i })
    const hotRadio = screen.getByRole('radio', { name: /warm|hot|warm\/hot/i })
    const submit = screen.getByRole('button', { name: /let's go!|let's go|go/i })

    await user.click(snowRadio)
    await user.click(hotRadio)

    expect(screen.getByText(/the weather doesn't work like that/i)).toBeInTheDocument()
    expect(submit).toBeDisabled()
  })

  it('enables submit with valid weather combinations and calls onSubmit', async () => {
    const user = userEvent.setup()
    const handleSubmit = vi.fn()
    render(<Form onSubmit={handleSubmit} />)

    const snowRadio = screen.getByRole('radio', { name: /snow/i })
    const coldRadio = screen.getByRole('radio', { name: /cool|cold|cool\/cold/i })
    const submit = screen.getByRole('button', { name: /let's go!|let's go|go/i })

    await user.click(snowRadio)
    await user.click(coldRadio)

    expect(screen.queryByText(/the weather doesn't work like that/i)).not.toBeInTheDocument()
    expect(submit).not.toBeDisabled()

    await user.click(submit)

    expect(handleSubmit).toHaveBeenCalled()
    const calledWith = handleSubmit.mock.calls[0][0]
    expect(calledWith).toHaveProperty('precipitation')
    expect(calledWith).toHaveProperty('temperature')
    expect(calledWith.precipitation).toBe('snow')
    expect(calledWith.temperature).toBe('cold')
  })
  
})