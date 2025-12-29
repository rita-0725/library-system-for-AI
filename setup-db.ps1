[string]$connectionString = "Server=localhost;User Id=root;Password=123456;"
[string]$database = "library_db"

# Load MySQL .Net Data Provider
[void][System.Reflection.Assembly]::LoadWithPartialName("MySql.Data")

# Create connection
$connection = New-Object MySql.Data.MySqlClient.MySqlConnection($connectionString)
$connection.Open()

# Read SQL file
$sqlScript = Get-Content "d:\Rita的college\大三上\软件工程\图书管理系统\backend\sql\tables.sql" -Raw

# Split by ; to get individual commands
$commands = $sqlScript -split "(?<=[;])\s*(?=\n|$)" | Where-Object { $_.Trim() -ne "" }

foreach ($command in $commands) {
    $cmd = $connection.CreateCommand()
    $cmd.CommandText = $command.Trim()
    
    try {
        $cmd.ExecuteNonQuery() | Out-Null
        Write-Host "✓ Executed: $($command.Trim().Substring(0, [Math]::Min(50, $command.Trim().Length)))..."
    }
    catch {
        Write-Host "✗ Error: $($_.Exception.Message)"
    }
}

$connection.Close()
Write-Host "Database setup completed!"