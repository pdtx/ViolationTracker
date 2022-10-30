#!/bin/bash

projectName=${1}
authSonar=${2}

curl -X POST -H "Authorization: Basic ${authSonar}" -H "Cache-Control: no-cache" -H "Postman-Token: 10a0e9a1-8dae-a9d1-45f2-0d8e56de999d" -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" -F "projects=${projectName}" "http://localhost:9000/api/projects/bulk_delete"