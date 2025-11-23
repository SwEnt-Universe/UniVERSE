@echo off
setlocal
:: This batch file launches the embedded PowerShell script below.
:: passing the script path safely via environment variable.
set "SCRIPT_PATH=%~f0"
powershell -NoProfile -ExecutionPolicy Bypass -Command "$p = $Env:SCRIPT_PATH; Invoke-Expression ((Get-Content $p -Raw) -replace '(?s).*?#POWERSHELL_START', '')"
exit /b

#POWERSHELL_START
# ---------------------------------------------------------------------------
# PowerShell Logic Begins Here
# ---------------------------------------------------------------------------

$logFile = "firebase-emulator.log"
$components = "auth,firestore"
$ports = @(8080, 9099)

# Global flag to control the main loop
$script:running = $true

# ---------------------------------------------------------------------------
# 1. Handle Ctrl+C Gracefully
# ---------------------------------------------------------------------------
# This prevents the immediate crash, allowing us to run cleanup.
# The actual suppression of the "Y/N" prompt happens in the 'finally' block.
[Console]::TreatControlCAsInput = $false
$cancelHandler = {
    param($sender, $e)
    $e.Cancel = $true
    $script:running = $false
    Write-Host "`nReceived stop signal. Cleaning up..." -ForegroundColor Yellow
}
[Console]::CancelKeyPress += $cancelHandler

# Clear console and set title
$Host.UI.RawUI.WindowTitle = "Firebase Emulator Automation"
Clear-Host

Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
Write-Host " Starting Firebase Emulators ($components)" -ForegroundColor Cyan
Write-Host "--------------------------------------------------------" -ForegroundColor Cyan

# Clean up old log file
if (Test-Path $logFile) { Remove-Item $logFile -Force }
"" | Set-Content $logFile

# ---------------------------------------------------------------------------
# 2. Start Emulator Process
# ---------------------------------------------------------------------------
$pInfo = New-Object System.Diagnostics.ProcessStartInfo
$pInfo.FileName = "cmd.exe"
# Removed --project argument here
$pInfo.Arguments = "/c firebase emulators:start --only $components > $logFile 2>&1"
$pInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Hidden
$pInfo.CreateNoWindow = $true
$pInfo.UseShellExecute = $false

$emulatorProcess = New-Object System.Diagnostics.Process
$emulatorProcess.StartInfo = $pInfo

$streamReader = $null

try {
    $started = $emulatorProcess.Start()
    if (-not $started) {
        Write-Error "Failed to start emulator process."
        exit 1
    }
    $emulatorPid = $emulatorProcess.Id
    Write-Host "`n-> Emulator launched (PID: $emulatorPid). Logs redirecting to $logFile" -ForegroundColor Gray

    # ---------------------------------------------------------------------------
    # 3. Wait for Ports to be Ready
    # ---------------------------------------------------------------------------
    Write-Host "-> Waiting for ports to open..." -ForegroundColor Gray

    foreach ($port in $ports) {
        if (-not $script:running) { break }
        Write-Host -NoNewline "   Checking port $port... "
        $retries = 0
        $connected = $false

        while (-not $connected -and $script:running) {
            try {
                $tcp = New-Object System.Net.Sockets.TcpClient
                $tcp.Connect("localhost", $port)
                $tcp.Close()
                $connected = $true
            } catch {
                Start-Sleep -Seconds 1
                $retries++
                if ($retries -gt 30) {
                    Write-Host "Timeout!" -ForegroundColor Red
                    throw "Timeout waiting for port $port"
                }
            }
        }
        if ($script:running) { Write-Host "Ready." -ForegroundColor Green }
    }

    if ($script:running) {
        Write-Host "`nEmulators are fully running." -ForegroundColor Green
        Write-Host "Listening for verification links... (Press Ctrl+C to Stop)" -ForegroundColor Yellow
        Write-Host "--------------------------------------------------------"
    }

    # ---------------------------------------------------------------------------
    # 4. Monitor Log Loop (Manual Tail Implementation)
    # ---------------------------------------------------------------------------

    # Wait for file to be created if it doesn't exist yet
    while (-not (Test-Path $logFile) -and $script:running) { Start-Sleep -Milliseconds 200 }

    if ($script:running) {
        $fileStream = New-Object System.IO.FileStream($logFile, [System.IO.FileMode]::Open, [System.IO.FileAccess]::Read, [System.IO.FileShare]::ReadWrite)
        $streamReader = New-Object System.IO.StreamReader($fileStream)
        $fileStream.Seek(0, [System.IO.SeekOrigin]::End) | Out-Null

        while ($script:running) {
            $line = $streamReader.ReadLine()

            if ($line -ne $null) {
                if ($line -match 'http://127.0.0.1:9099/emulator/action\?mode=verifyEmail.*') {
                    $url = $matches[0]
                    Write-Host "-> Clicking verification link: $url" -ForegroundColor Cyan
                    try {
                        $resp = Invoke-WebRequest -Uri $url -UseBasicParsing -Method Get
                        Write-Host "   Success: $($resp.StatusCode)" -ForegroundColor Green
                    } catch {
                        Write-Host "   Error clicking link: $_" -ForegroundColor Red
                    }
                }
            } else {
                Start-Sleep -Milliseconds 100
            }

            if ($emulatorProcess.HasExited) {
                Write-Host "`nEmulator process exited unexpectedly." -ForegroundColor Red
                $script:running = $false
            }
        }
    }
}
catch {
    Write-Host "`nError: $_" -ForegroundColor Red
}
finally {
    # ---------------------------------------------------------------------------
    # 5. CLEANUP SECTION
    # ---------------------------------------------------------------------------

    if ($streamReader) { $streamReader.Close(); $streamReader.Dispose() }

    Write-Host " Shutting down..." -ForegroundColor Yellow

    if ($emulatorPid) {
        $killProc = Start-Process "taskkill" -ArgumentList "/PID $emulatorPid /T /F" -NoNewWindow -PassThru -Wait
        Write-Host " Emulator process tree killed." -ForegroundColor Green
    }

    Write-Host "--------------------------------------------------------"
    Start-Sleep -Milliseconds 500

    # CRITICAL: Kill the parent CMD process to prevent "Terminate batch job (Y/N)?"
    # This forces a silent exit of the window/terminal.
    try {
        $myPid = $PID
        $parentQuery = "SELECT ParentProcessId FROM Win32_Process WHERE ProcessId = $myPid"
        $parentProcId = (Get-WmiObject -Query $parentQuery).ParentProcessId

        if ($parentProcId) {
            # We only kill if the parent is actually cmd.exe (to be safe)
            $parentProc = Get-Process -Id $parentProcId -ErrorAction SilentlyContinue
            if ($parentProc -and $parentProc.ProcessName -eq "cmd") {
                Stop-Process -Id $parentProcId -Force -ErrorAction SilentlyContinue
            }
        }
    } catch {
        # Ignore errors if parent is already gone
    }
}