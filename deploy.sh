#!/bin/bash

# --- 設定部分 ---
KEY_PATH="C:/Users/kmnsx/Downloads/assetmanager-web.pem"   # ←ここ、あなたの.pemパス！
SERVER_IP="52.69.162.182"
SSH_USER="ec2-user"
JAR_PATH="target/asset-manager-0.0.1-SNAPSHOT.jar"
REMOTE_DIR="/home/ec2-user"

# --- 処理部分 ---
echo "1. ローカルでMavenビルド開始..."
./mvnw clean package
if [ $? -ne 0 ]; then
  echo "ビルドに失敗しました。スクリプトを終了します。"
  exit 1
fi
echo "ビルド成功！"

echo "2. サーバーにjarファイルを送信中..."
scp -i "$KEY_PATH" "$JAR_PATH" "$SSH_USER@$SERVER_IP:$REMOTE_DIR/"
if [ $? -ne 0 ]; then
  echo "scp失敗。スクリプトを終了します。"
  exit 1
fi
echo "scp成功！"

echo "3. サーバーにSSH接続してDocker再起動..."
ssh -i "$KEY_PATH" "$SSH_USER@$SERVER_IP" << EOF
  cd $REMOTE_DIR
  docker-compose down
  docker-compose up --build -d
EOF

echo "デプロイ完了！！！🚀"
