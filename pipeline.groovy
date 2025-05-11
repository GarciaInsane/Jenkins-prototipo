pipeline {
    agent any

    environment {
        GIT_REPO   = 'https://github.com/GarciaInsane/PruebaCompilacion.git'
        GIT_BRANCH = 'main'
        BUILD_DIR  = 'build'
    }

    stages {
        stage('Clonar repositorio') {
            steps {
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO}"
            }
        }

        stage('Compilar') {
          steps {
            powershell '''
              Write-Host "Usando Java en $Env:JAVA_HOME"
              if (-Not (Test-Path build)) { New-Item -ItemType Directory -Name build }
              Get-ChildItem -Recurse -Filter *.java src | ForEach-Object {
                Write-Host "Compilando $($_.FullName)"
                & "$Env:JAVA_HOME\\bin\\javac.exe" -d build $_.FullName
              }
            '''
          }
        }

        stage('Ejecutar') {
          steps {
            powershell '''
              Write-Host "=== Ejecutando clase Main desde $Env:BUILD_DIR ==="
        
              $mainClassPath = Join-Path $Env:BUILD_DIR "unad\\project\\Main.class"
        
              if (-Not (Test-Path $mainClassPath)) {
                Write-Warning "⚠️ No se encontró Main.class en: $mainClassPath"
                exit 1
              }
        
              Write-Host "✅ Main.class encontrado. Ejecutando..."
        
              Push-Location $Env:BUILD_DIR
              & "$Env:JAVA_HOME\\bin\\java.exe" unad.project.Main
              Pop-Location
            '''
          }
        }

        stage('Limpiar workspace') {
            steps {
                cleanWs()
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline completado con éxito.'
        }
        failure {
            echo '❌ Hubo errores en el pipeline.'
        }
    }
}
