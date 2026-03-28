import { useEffect, useState } from "react"
import { useParams, Link } from "react-router"
import { Download, File, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import axios from "axios"

interface FileInfo {
  id: number
  fileName: string
  fileType: string
  fileSize: number
  objectKey: string
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 Bytes"
  const k = 1024
  const sizes = ["Bytes", "KB", "MB", "GB"]
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + " " + sizes[i]
}

export default function Share() {
  const { token } = useParams<{ token: string }>()
  const [file, setFile] = useState<FileInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [downloading, setDownloading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchFileInfo = async () => {
      try {
        const res = await axios.get(`/api/share/${token}`)
        setFile(res.data)
      } catch (err) {
        setError("File not found or link is invalid")
      } finally {
        setLoading(false)
      }
    }
    fetchFileInfo()
  }, [token])

  const handleDownload = async () => {
    if (!token) return
    setDownloading(true)
    try {
      const res = await axios.get(`/api/share/${token}/download`)
      window.location.href = res.data
    } catch (err) {
      alert("Failed to generate download link")
    } finally {
      setDownloading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex min-h-[calc(100vh-3.5rem)] items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-[calc(100vh-3.5rem)] items-center justify-center">
        <Card className="w-[400px]">
          <CardHeader>
            <CardTitle className="text-destructive">Error</CardTitle>
            <CardDescription>{error}</CardDescription>
          </CardHeader>
          <CardContent>
            <Link to="/">
              <Button variant="outline" className="w-full">Go to Home</Button>
            </Link>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="flex min-h-[calc(100vh-3.5rem)] items-center justify-center p-4">
      <Card className="w-[400px]">
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-primary/10">
              <File className="h-6 w-6 text-primary" />
            </div>
            <div>
              <CardTitle className="text-xl">{file?.fileName}</CardTitle>
              <CardDescription>{file?.fileType || "Unknown type"}</CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Size</span>
            <span className="font-medium">{file?.fileSize ? formatFileSize(file.fileSize) : "Unknown"}</span>
          </div>
          <Button 
            className="w-full" 
            onClick={handleDownload}
            disabled={downloading}
          >
            {downloading ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Download className="mr-2 h-4 w-4" />
            )}
            Download File
          </Button>
          <Link to="/" className="block">
            <Button variant="outline" className="w-full">Go to DropSpace</Button>
          </Link>
        </CardContent>
      </Card>
    </div>
  )
}
