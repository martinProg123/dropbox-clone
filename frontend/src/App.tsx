import { useEffect, useState } from 'react'
import { Outlet, NavLink, useNavigate } from "react-router";
import { Upload } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Field,
  FieldGroup,
  FieldLabel,
} from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Toaster } from "sonner"
import api from '@/lib/axios';
import axios from 'axios';

const FORM_STATUS = {
  LOGIN: 'login',
  SIGNUP: 'signup',
} as const;
const MINPWLEN = 8;

const formLogicObj = {
  [FORM_STATUS.LOGIN]: {
    url: '/api/auth/login',
    btnText: 'Login',
  },
  [FORM_STATUS.SIGNUP]: {
    url: '/api/auth/register',
    btnText: 'Sign up',
  },
}
function App() {

  const [isLogin, setIsLogin] = useState(false)
  const [isOpen, setIsOpen] = useState(false)
  const [formType, setFormType] = useState("")
  const [email, setEmail] = useState("")
  const [pw, setPw] = useState("")
  const [rpw, setRpw] = useState("")
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/api/auth/me')
      .then(() => setIsLogin(true))
      .catch(() => setIsLogin(false))
  }, [])

  const handleAuthUI = () => {
    if (!isLogin) {
      showLoginForm()
    }
    else {
      logOut()
    }
  }
  const logOut = async () => {
    try {
      const response = await api.post("/api/auth/logout");
      console.log(response.data); // The response data from the server
      toast.success("Logout success")
      setIsLogin(false)
      navigate("/")
    } catch (error) {
      console.error(error); // Handles any errors during the request
      toast.error("Something wrong. Try again later")
    }
  }

  const showLoginForm = () => {
    setFormType(FORM_STATUS.LOGIN)
    setIsOpen(true)
  }

  const showSignUpForm = () => {
    setFormType(FORM_STATUS.SIGNUP)
    setIsOpen(true)
  }

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!email) {
      toast.error("Please enter your email")
      return
    }

    if (!pw) {
      toast.error("Please enter your password")
      return
    }

    if (pw.length < MINPWLEN) {
      toast.error(`Password must be at least ${MINPWLEN} characters`)
      return
    }

    const url = formType === FORM_STATUS.SIGNUP 
      ? formLogicObj[FORM_STATUS.SIGNUP].url 
      : formLogicObj[FORM_STATUS.LOGIN].url
    
    const data = formType === FORM_STATUS.SIGNUP
      ? { email, password: pw, confirmPw: rpw }
      : { email, password: pw }

    try {
      const response = await api.post(url, data);
      console.log(response.data); // The response data from the server
      toast.success(formType === FORM_STATUS.LOGIN ? "Login successful!" : "Sign up successful!")
      navigate("/space")
    } catch (error) {
      console.error(error); // Handles any errors during the request
      if (axios.isAxiosError(error)) {
        const msg = error.response?.data || "Something wrong. Try again later"
        toast.error(msg)
      } else {
        toast.error("Something wrong. Try again later")
      }
      return
    }
    setIsLogin(true)
    setEmail("")
    setPw("")
    setRpw("")
    setIsOpen(false)
  }

  return (
    <div className="">
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto flex h-14 items-center justify-between px-4">
          <nav className="flex items-center gap-2">
            <NavLink to="/" className="flex items-center gap-2">
              <div className="flex h-8 w-8 items-center justify-center rounded-md bg-primary">
                <Upload className="h-4 w-4 text-primary-foreground" />
              </div>
              <span className="text-lg font-bold">DropSpace</span>
            </NavLink>
          </nav>
          <Button
            variant={isLogin ? "destructive" : "default"}
            className="cursor-pointer"
            onClick={handleAuthUI}
          >
            {isLogin ? "Logout" : "Login"}
          </Button>
        </div>
      </header>

      <main className="min-h-[calc(100vh-3.5rem-3rem)] bg-background">
        <Outlet context={{ showLoginForm, showSignUpForm }} />
      </main>

      <footer className="border-t py-6">
        <p className="mx-auto px-4 text-center text-sm text-muted-foreground">
          By{" "}
          <a href="https://github.com/martinProg123" className="hover:underline">Martin</a>{" "}
          {new Date().getFullYear()}
        </p>
      </footer>

      <Dialog open={isOpen} onOpenChange={setIsOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>{
              formType == FORM_STATUS.LOGIN
                ? formLogicObj[FORM_STATUS.LOGIN].btnText
                : formLogicObj[FORM_STATUS.SIGNUP].btnText
            }</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="email">Email</FieldLabel>
                <Input className='' id="email" type='email' autoComplete="off" placeholder="m@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
                {/* <FieldError>Invalid Email</FieldError> */}
              </Field>
              <Field>
                <FieldLabel htmlFor="password">Password</FieldLabel>
                <Input id="password" type='password' autoComplete="off"
                  value={pw}
                  onChange={(e) => setPw(e.target.value)}
                />
              </Field>
              {
                formType == FORM_STATUS.SIGNUP &&
                (<Field>
                  <FieldLabel htmlFor="rpassword">Confirmed Password</FieldLabel>
                  <Input id="rpassword" type='password' autoComplete="off"
                    value={rpw}
                    onChange={(e) => setRpw(e.target.value)} />
                </Field>)
              }
              <Field className="flex flex-row gap-2">
                <Button type="submit" className="flex-1 font-semibold">
                  {
                    formType == FORM_STATUS.LOGIN
                      ? formLogicObj[FORM_STATUS.LOGIN].btnText
                      : formLogicObj[FORM_STATUS.SIGNUP].btnText
                  }
                </Button>
                {formType == FORM_STATUS.LOGIN &&
                  (<Button
                    variant="outline"
                    className="flex-1 font-semibold"
                    onClick={(e) => {
                      setFormType(FORM_STATUS.SIGNUP)
                      e.preventDefault();
                    }}
                  >{formLogicObj[FORM_STATUS.SIGNUP].btnText}</Button>)
                }
              </Field>
            </FieldGroup>
          </form>
        </DialogContent>
      </Dialog>

      <Toaster position="top-center" richColors />
    </div>
  )
}

export default App
