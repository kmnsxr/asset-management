# ①ベースイメージ（OpenJDK）
FROM openjdk:17-jdk-slim

# ②作業ディレクトリ作成
WORKDIR /app

# ③jarファイルをコンテナにコピー
COPY target/*.jar app.jar

# ④アプリケーションを実行
ENTRYPOINT ["java", "-jar", "app.jar"]
