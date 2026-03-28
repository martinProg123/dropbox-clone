import { Upload, Zap, Share2, Search } from "lucide-react"
import { useOutletContext } from "react-router"
import { Button } from "@/components/ui/button"
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"

const features = [
  {
    icon: Upload,
    title: "Simple Uploads",
    description: "Drag and drop your files with ease. Support for large files up to 5GB.",
  }, 
  {
    icon: Zap,
    title: "Lightning Fast",
    description: "Upload and download speeds that won't slow you down.",
  },
  {
    icon: Share2,
    title: "Easy Sharing",
    description: "Share files with anyone using secure, expiring links.",
  },
  {
    icon: Search,
    title: "Full-Text Search",
    description: "Find any file instantly with powerful search capabilities.",
  },
]
type AuthContext = {
  showLoginForm: () => void
  showSignUpForm: () => void
}

function Home() {
  const { showLoginForm, showSignUpForm } = useOutletContext<AuthContext>()
  return (
    <div className="">
        <section className="py-24 md:py-28">
          <div className="container mx-auto px-4 text-center">
            <h1 className="mb-6 text-4xl font-bold tracking-tight md:text-6xl">
              Your files,{" "}
              <span className="text-primary">secure and accessible</span>
            </h1>
            <p className="mx-auto mb-8 max-w-2xl text-lg text-muted-foreground">
              Store, share, and search your files. 
            </p>
            <div className="flex flex-col items-center justify-center gap-4 sm:flex-row">
              <Button size="lg" onClick={showSignUpForm}>
                Start Today
              </Button>
              <Button size="lg" variant="outline" onClick={showLoginForm}>
                Login
              </Button>
            </div>
          </div>
        </section>

        <section className="py-16">
          <div className="container mx-auto px-4">
            <div className="mb-12 text-center">
              <h2 className="mb-4 text-3xl font-bold tracking-tight">
                Everything you need
              </h2>
              <p className="text-muted-foreground">
                Powerful features to help you manage your files
              </p>
            </div>
            <div className="grid gap-6 md:grid-cols-1 lg:grid-cols-2">
              {features.map((feature) => (
                <Card key={feature.title} className="border-dashed">
                  <CardHeader>
                    <div className="mb-2 flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
                      <feature.icon className="h-5 w-5 text-primary" />
                    </div>
                    <CardTitle>{feature.title}</CardTitle>
                    <CardDescription>{feature.description}</CardDescription>
                  </CardHeader>
                </Card>
              ))}
            </div>
          </div>
        </section>
    </div>
  )
}

export default Home
