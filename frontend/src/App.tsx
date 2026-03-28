import { useState } from 'react'
import { Outlet, NavLink, useNavigate } from "react-router";
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Field,
  FieldDescription,
  FieldError,
  FieldGroup,
  FieldLabel,
  FieldSeparator,
  FieldSet,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"

function App() {
  const [isLogin, setIsLogin] = useState(false)
  const [isOpen, setIsOpen] = useState(false)
  const navigate = useNavigate();

  const handleAuthUI = () => {
    if (!isLogin) setIsOpen(true)
    else {
      logOut()
    }
  }
  const logOut = () => {
    setIsLogin(false)
    navigate("/")
  }

  return (
    <div className="">
      <header className="flex justify-between p-5">
        <nav>
          <NavLink to="/">Home</NavLink> | <NavLink to="/space">File</NavLink>
        </nav>
        <Button variant="outline"
        className='cursor-pointer '
          onClick={handleAuthUI}
        >
          {isLogin ? "Logout" : "Login"}
        </Button>
      </header>

      <main className="min-h-full">
        <Outlet />
      </main>

      <footer>
        <p>
          By 
          <a href="https://github.com/martinProg123">Martin</a>
        </p>
      </footer>

      <Dialog open={isOpen} onOpenChange={setIsOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Log In</DialogTitle>
          </DialogHeader>
          <form onSubmit={(event) => {
            event.preventDefault()
            setIsOpen(false)
          }}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="email">Email</FieldLabel>
                <Input id="email" type='email' autoComplete="off" placeholder="m@email.com" />
                <FieldError>Invalid Email</FieldError>
              </Field>
              <Field>
                <FieldLabel htmlFor="password">Password</FieldLabel>
                <Input id="password" type='password' autoComplete="off" aria-invalid />
              </Field>
              <Field>
                <Button type="submit">LogIn</Button>
                <Button >Sign Up</Button>
              </Field>
            </FieldGroup>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}

export default App
