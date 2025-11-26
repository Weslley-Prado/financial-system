# ============================================
# Itaú Transfer API - Dockerfile
# Multi-stage build para imagem otimizada
# ============================================

# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# Copiar arquivos do Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Baixar dependências (cache layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine

# Metadados
LABEL maintainer="Itaú Transfer API Team"
LABEL version="1.0.0"
LABEL description="API de Transferências Bancárias - Case Técnico Itaú"

# Criar usuário não-root para segurança
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -h /app -D appuser

WORKDIR /app

# Copiar JAR do stage de build
COPY --from=builder /app/target/*.jar app.jar

# Alterar ownership
RUN chown -R appuser:appgroup /app

# Mudar para usuário não-root
USER appuser

# Variáveis de ambiente padrão
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication"
ENV SPRING_PROFILES_ACTIVE=prod

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8881/actuator/health || exit 1

# Expor porta
EXPOSE 8881

# Entrypoint
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]


