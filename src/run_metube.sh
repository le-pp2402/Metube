#!/bin/bash

# Load environment variables from the .env file
export $(grep -v '^#' .env | xargs)

# Path to your JAR file
JAR_FILE="/home/o/Metube/Metube/target/Mecube-0.0.1-SNAPSHOT.jar"

# Check if the JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file '$JAR_FILE' not found!"
    exit 1
fi

# Run the JAR file with all environment variables as arguments
java -jar "$JAR_FILE" \
  --DB_SOURCE="jdbc:mysql://localhost:3306/Metube" \
  --DB_USERNAME="root" \
  --DB_PASSWORD="root" \
  --MAIL_HOST="smtp.gmail.com" \
  --MAIL_POST="587" \
  --MAIL_USERNAME="lephiphatphat@gmail.com" \
  --MAIL_PASSWORD="tpgp lxfo vuqa xpdi" \
  --SECRET_KEY="zEQ66eVWeiL0DBGylYSWATWkRe0KAIcmM6hs12AfgcJBMP1Wf7iBHf1xQN2dC+p+" \
  --EXPIRATION_TIME="259200000" \
  --MinIO_ACCESS_KEY="ALqKIBs94HWpsLskAJ1D" \
  --MinIO_SECRET_KEY="OCuwv2nu7629u1E2YW6dJcFi6TGSLHjg0ZnxCHD6" \
  --MinIO_URL="http://127.0.0.1:9000" \
  --BUCKET_NAME="resources" \
  --MeliSearch_MASTER_KEY="HziFkipe1UqaYjQXSG0K697d7bCpMGAti4enBo9cRdk" \
  --MeliSearch_PORT="http://localhost:7700/" \
  --GOOGLE_API="https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" \
  --GOOGLE_KEY="AIzaSyBN_4DX01puv4xHBv4PHr0LqRDsEoYeVZw" \
  --WHISPER_API="http://localhost:9003/sub/" \
  --NEO4J_URI="bolt://localhost:7687" \
  --NEO4J_USERNAME="neo4j" \
  --NEO4J_PASSWORD="learnvocabulary" \
  --NEO4J_DB="learnvocabulary" \
  --FOLDER_UPLOAD="~/Metube/tmp/" \
  --QUEUE_HOST="172.20.10.3"
