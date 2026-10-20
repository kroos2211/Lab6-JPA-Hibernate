# Produit des images d'extraits textuels issus exclusivement des vrais journaux Maven.
# Ces PNG ne sont pas des captures du terminal integre.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$resultats = Join-Path $root 'resultats'
$captures = Join-Path $root 'captures'
New-Item -ItemType Directory -Force -Path $captures | Out-Null

function Select-Lines([string]$file, [string[]]$patterns) {
    $lines = Get-Content -LiteralPath (Join-Path $resultats $file) -Encoding UTF8
    $selected = New-Object System.Collections.Generic.List[string]
    foreach ($pattern in $patterns) {
        $matches = @($lines | Where-Object { $_ -match $pattern })
        if ($matches.Count -eq 0) { throw "Aucune ligne pour '$pattern' dans $file" }
        foreach ($match in $matches) { $selected.Add($match) }
    }
    return $selected.ToArray()
}

function Make-Image([string]$name, [string]$source, [string]$command, [string[]]$patterns) {
    $lines = @(Select-Lines $source $patterns)
    $wrapped = New-Object System.Collections.Generic.List[string]
    foreach ($line in $lines) {
        $remaining = $line
        while ($remaining.Length -gt 135) {
            $wrapped.Add($remaining.Substring(0, 135))
            $remaining = $remaining.Substring(135)
        }
        $wrapped.Add($remaining)
    }
    $height = [Math]::Max(260, 118 + 27 * $wrapped.Count)
    $bitmap = New-Object System.Drawing.Bitmap(1600, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $font = New-Object System.Drawing.Font('Consolas', 14)
    try {
        $graphics.Clear([System.Drawing.Color]::FromArgb(18, 22, 30))
        $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAlias
        $graphics.DrawString("Source : resultats/$source", $font, [System.Drawing.Brushes]::LightSkyBlue, 27, 18)
        $graphics.DrawString("Commande : $command", $font, [System.Drawing.Brushes]::LightSkyBlue, 27, 44)
        $graphics.DrawString('Lignes selectionnees, reproduites sans modification :', $font, [System.Drawing.Brushes]::Silver, 27, 70)
        $y = 105
        foreach ($line in $wrapped) {
            $graphics.DrawString($line, $font, [System.Drawing.Brushes]::White, 27, $y)
            $y += 27
        }
        $bitmap.Save((Join-Path $captures $name), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $font.Dispose(); $graphics.Dispose(); $bitmap.Dispose()
    }
}

Make-Image '01-compilation.png' '01-compilation.txt' 'mvn clean compile' @('Compiling 16 source files', '^\[INFO\] BUILD SUCCESS$')
Make-Image '02-tests-junit.png' '02-tests-junit.txt' 'mvn clean test' @('^\[INFO\] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0$', '^\[INFO\] BUILD SUCCESS$')
Make-Image '03-categories-produits.png' '04-application.txt' 'mvn exec:java' @('^=== CREATION DES CATEGORIES ===$', '^Categorie\{', '^=== CREATION DES PRODUITS ===$', '^Produit\{')
Make-Image '04-crud.png' '04-application.txt' 'mvn exec:java' @('^=== OPERATIONS CRUD ===$', '^Create :', '^Read :', '^Update :', '^Delete :')
Make-Image '05-named-queries.png' '04-application.txt' 'mvn exec:java' @('^=== NAMED QUERIES ===$', '^findAll :', '^findByCategorie', '^searchByLibelle')
Make-Image '06-criteria-api.png' '04-application.txt' 'mvn exec:java' @('^=== RECHERCHE CRITERIA API ===$', '^Nom ', '^Prix minimum', '^Prix maximum', '^Combinee', '^Sans filtre')
Make-Image '07-pagination.png' '04-application.txt' 'mvn exec:java' @('^=== PAGINATION PAGE 1 ===$', '^Total=6, page 1=', '^=== PAGINATION PAGE 2 ===$', '^Total=6, page 2=')
Make-Image '08-heritage-joined.png' '04-application.txt' 'mvn exec:java' @('^=== HERITAGE JOINED ===$', '^Client\{', '^Fournisseur\{', '^Recherche polymorphique')
Make-Image '09-commandes-relations.png' '04-application.txt' 'mvn exec:java' @('^=== CREATION DES COMMANDES ===$', '^Commande\{', '^=== RELATION N:N ET CLE COMPOSEE ===$', '^Produit=')
Make-Image '10-rapport-build.png' '04-application.txt' 'mvn exec:java' @('^=== RAPPORT DES VENTES MENSUELLES ===$', '^\d{4}-\d{2} \|', '^\[INFO\] BUILD SUCCESS$')

$appLines = Get-Content -LiteralPath (Join-Path $resultats '04-application.txt') -Encoding UTF8
$rapportLines = @($appLines | Where-Object { $_ -match '^=== RAPPORT DES VENTES MENSUELLES ===$|^\d{4}-\d{2} \||^\[INFO\] BUILD SUCCESS$' })
if ($rapportLines.Count -lt 3) { throw 'Rapport incomplet dans le journal' }
@('Extrait de resultats/04-application.txt (lignes selectionnees sans modification)') + $rapportLines |
    Set-Content -LiteralPath (Join-Path $resultats '05-rapport-ventes.txt') -Encoding UTF8

Get-ChildItem -LiteralPath $captures -Filter '*.png' | Select-Object Name, Length
