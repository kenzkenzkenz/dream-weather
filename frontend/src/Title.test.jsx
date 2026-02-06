import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import Title from './Title'

describe('Title', () => {
  it('renders the title text', () => {
    render(<Title />)
    const title = screen.getByText('Dream Weather')
    expect(title).toBeInTheDocument()
  })

  it('renders the logo image', () => {
    render(<Title />)
    const logo = screen.getByAltText('Logo')
    expect(logo).toBeInTheDocument()
  })

  it('applies correct CSS classes', () => {
    const { container } = render(<Title />)
    const header = container.querySelector('.header')
    const logo = container.querySelector('.logo')
    const title = container.querySelector('.title')
    
    expect(header).toBeInTheDocument()
    expect(logo).toBeInTheDocument()
    expect(title).toBeInTheDocument()
  })

  it('logo has correct src attribute', () => {
    render(<Title />)
    const logo = screen.getByAltText('Logo')
    expect(logo).toHaveAttribute('src')
  })
})
