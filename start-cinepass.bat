@echo off
setlocal
set JAVA_TOOL_OPTIONS=-Dnet.bytebuddy.experimental=true
set EUREKA_INSTANCE_HOSTNAME=localhost
set EUREKA_INSTANCE_PREFER_IP_ADDRESS=false
set MVN=%~dp0tools\apache-maven-3.9.9\bin\mvn.cmd
cd /d %~dp0

start "CinePass Eureka" cmd /k ""%MVN%" -f backend\pom.xml -pl eureka-server spring-boot:run"
timeout /t 12 /nobreak >nul
start "CinePass Gateway" cmd /k ""%MVN%" -f backend\pom.xml -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=prod"
start "CinePass Users" cmd /k ""%MVN%" -f backend\pom.xml -pl user-service spring-boot:run -Dspring-boot.run.profiles=prod"
start "CinePass Movies" cmd /k ""%MVN%" -f backend\pom.xml -pl movie-service spring-boot:run -Dspring-boot.run.profiles=prod"
start "CinePass Shows" cmd /k ""%MVN%" -f backend\pom.xml -pl show-service spring-boot:run -Dspring-boot.run.profiles=prod"
start "CinePass Bookings" cmd /k ""%MVN%" -f backend\pom.xml -pl booking-service spring-boot:run -Dspring-boot.run.profiles=prod"

cd frontend
if not exist node_modules call npm install
start "CinePass Web" cmd /k npm run dev
echo CinePass is starting. Open http://localhost:5173
echo Eureka dashboard: http://localhost:8761
echo Staff check-in: http://localhost:5173/check-in
endlocal
