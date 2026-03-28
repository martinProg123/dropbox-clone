import { useCallback, useEffect, useState, useRef } from "react"
import { useDropzone } from "react-dropzone"
import { Search, Upload, MoreHorizontal, FileIcon, Download, Trash2, Share2 } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import api from '@/lib/axios';
import axios from 'axios';
import { useNavigate } from "react-router";
import { toast } from "sonner"

interface FileItem {
  id: number
  fileName: string
  fileType: string
  fileSize: number
  createdAt: string
  status: "UPLOADING" | "PROCESSING" | "COMPLETED" | "FAILED"
  objectKey: string
}

// const mockFiles: FileItem[] = [
//   { id: 1, fileName: "project-proposal.pdf", createdAt: "Mar 28, 2026", fileSize: "2.4 MB", status: "completed" },
//   { id: 2, fileName: "meeting-notes.docx", createdAt: "Mar 27, 2026", size: "156 KB", status: "completed" },
//   { id: 3, fileName: "budget-2026.xlsx", createdAt: "Mar 26, 2026", size: "890 KB", status: "processing" },
//   { id: 4, fileName: "presentation.pptx", createdAt: "Mar 25, 2026", size: "5.2 MB", status: "completed" },
// ]

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return "0 B"
  const k = 1024
  const sizes = ["B", "KB", "MB", "GB"]
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i]
}

const statusVariant = (status: FileItem["status"]) => {
  switch (status) {
    case "COMPLETED":
      return "default"
    case "PROCESSING":
      return "secondary"
    case "UPLOADING":
      return "outline"
    default:
      return "outline"
  }
}

function Space() {
  const navigate = useNavigate();
  const [userEmail, setUserEmail] = useState("")
  const [debouncedText, setDebText] = useState("")
  const [searchText, setSearchText] = useState("")
  const [userFileList, setUserFileList] = useState<FileItem[]>([])
  const pollingIntervals = useRef<Record<number, number>>({})

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      console.log(debouncedText)
      setSearchText(debouncedText);
    }, 500);

    return () => {
      clearTimeout(timeoutId);
    }
  }, [debouncedText])

  useEffect(() => {
    if (!searchText) {
      api.get("/api/files")
        .then((res) => {
          setUserFileList(res.data || [])
        })
        .catch((er) => {
          console.error(er)
        })
      return
    }
    api.get("/api/files/search", { params: { query: searchText } })
    .then((res)=>{
      if(res.data){
        setUserFileList(res.data)
      }
    }).catch((er)=>{
      console.error(er)
      toast.error("Search failed")
    })
  }, [searchText])


  useEffect(() => {
    const processingFiles = userFileList.filter(f => f.status === "PROCESSING")

    processingFiles.forEach(file => {
      if (!pollingIntervals.current[file.id]) {
        pollingIntervals.current[file.id] = setInterval(async () => {
          try {
            const res = await api.get(`/api/files/${file.id}`)
            const updatedFile = res.data
            setUserFileList(prev => prev.map(f =>
              f.id === file.id ? { ...f, status: updatedFile.status } : f
            ))
            if (updatedFile.status === "COMPLETED" || updatedFile.status === "FAILED") {
              clearInterval(pollingIntervals.current[file.id])
              delete pollingIntervals.current[file.id]
            }
          } catch (err) {
            clearInterval(pollingIntervals.current[file.id])
            delete pollingIntervals.current[file.id]
          }
        }, 2000)
      }
    })

    return () => {
      Object.values(pollingIntervals.current).forEach(interval => clearInterval(interval))
      pollingIntervals.current = {}
    } 
  }, [userFileList])

  useEffect(() => {
    api.get('/api/auth/me')
      .then((res) => {
        setUserEmail(res.data.email || "")

        api.get('/api/files')
          .then((fileListRes) => {
            console.log(fileListRes)
            setUserFileList(fileListRes.data || [])
          }).catch((err) => {
            toast.error("Failed to fetch files")
          })
      })
      .catch(() => {
        navigate("/")
      })
  }, [navigate])

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    if (acceptedFiles.length === 0) {
      toast.error("Choose a file")
      return
    }

    const file = acceptedFiles[0]
    toast.info("Uploading...")

    try {
      const initRes = await api.post('/api/upload/init', {
        fileName: file.name,
        fileSize: file.size
      })

      const { presignedUrl, fileId } = initRes.data

      await axios.put(presignedUrl, file)

      await api.post('/api/upload/complete', {
        fileId: fileId
      })

      toast.success("Upload complete! Processing...")

      const fileListRes = await api.get('/api/files')
      setUserFileList(fileListRes.data || [])
    } catch (err) {
      console.error(err)
      toast.error("Upload failed")
    }
  }, [])

  const genShareLink = async (fileId: number) => {
    try {
      const res = await api.post(`/api/files/${fileId}/share`)
      const sharedLink = res.data
      console.log(sharedLink)
      await navigator.clipboard.writeText(sharedLink)
      toast.success(`Share link ${sharedLink} copied!`)
    } catch (err) {
      toast.error("Generate share link failed")
    }
  }

  const downLoad = async (fileId: number) => {
    try {
      const res = await api.get(`/api/files/${fileId}/download`)
      const presignedUrl = res.data
      window.open(presignedUrl, '_blank')
    } catch (err) {
      toast.error("Download failed")
    }
  }

  const deleteFile = async (fileId: number) => {
    try {
      const res = await api.delete(`/api/files/${fileId}`)
      toast.success("File deleted")
      const fileListRes = await api.get('/api/files')
      setUserFileList(fileListRes.data || [])
    } catch (err) {
      toast.error("Delete file failed")
    }
  }

  const { getRootProps, getInputProps } = useDropzone({
    onDrop,
    multiple: false,
  })

  return (
    <div className="flex flex-col gap-6 p-6">
      <section className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Hello, {userEmail || "User"}!</h1>
        <div {...getRootProps()}>
          <input {...getInputProps()} />
          <Button asChild>
            <span className="cursor-pointer">
              <Upload className="size-4" />
              Upload
            </span>
          </Button>
        </div>
      </section>

      <section>
        <div className="relative">
          <Search className="absolute left-2.5 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input
            type="search"
            placeholder="Search files..."
            className="pl-9"
            value={debouncedText}
            onChange={(e) => setDebText(e.target.value)}
          />
        </div>
      </section>

      <section>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[40%]">File Name</TableHead>
              <TableHead>Created Date</TableHead>
              <TableHead>Size</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="w-[50px]"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {userFileList.map((file) => (
              <TableRow key={file.id}>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <FileIcon className="size-4 text-muted-foreground" />
                    <span className="font-medium">{file.fileName}</span>
                  </div>
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {file.createdAt}
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {formatFileSize(file.fileSize)}
                </TableCell>
                <TableCell>
                  <Badge variant={statusVariant(file.status)} className="capitalize">
                    {file.status}
                  </Badge>
                </TableCell>
                <TableCell>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="icon-xs">
                        <MoreHorizontal className="size-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => downLoad(file.id)}>
                        <Download className="size-4" />
                        Download
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => genShareLink(file.id)}>
                        <Share2 className="size-4" />
                        Share
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem className="text-destructive" onClick={() => deleteFile(file.id)}>
                        <Trash2 className="size-4" />
                        Delete
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </section>
    </div>
  )
}

export default Space
