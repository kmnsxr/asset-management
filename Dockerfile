# 1. OpenJDK の公式イメージを使用
FROM openjdk:17-jdk-slim

# 2. ワーキングディレクトリを設定
WORKDIR /app

# 3. Mavenのプロジェクトをビルドする（コンテナ内で）
COPY . /app
RUN mvn clean package

# 4. 作成されたJARファイルを実行
CMD ["java", "-Xms256m", "-Xmx1024m", "-XX:+UseG1GC", "-jar", "target/asset-manager-0.0.1-SNAPSHOT.jar"]
