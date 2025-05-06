https://github.com/le-pp2402/nginx-rtmp-module

Module NGINX-RTMP đã được chỉnh sửa, đi kèm với Dockerfile, docker-compose ....


![Module 1](https://github.com/user-attachments/assets/1cf31326-dd86-4ae5-ac56-41bd59da65ab)

🎬 Video Upload and Transcoding Flow
1. The user sends a request to upload a video file, including a valid authentication token.

2. The Main Service requests a pre-signed URL from Amazon S3.

3. S3 responds with the generated pre-signed URL.

4. The Main Service returns the pre-signed URL to the user.

5. The user uploads the video file directly to S3 using the pre-signed URL.

6. After the upload completes, S3 triggers an event that sends a message to RabbitMQ.

7. The Transcoding Service listens to the queue, consumes the message, and transcodes the video into HLS format (.m3u8 and .ts files).

8. The transcoded HLS files are uploaded back to S3, ready to be served to end users.
