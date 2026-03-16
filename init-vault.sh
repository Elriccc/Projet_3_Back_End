set -e

echo "Waiting for Vault..."

until vault status >/dev/null 2>&1
do
  sleep 2
done

echo "Vault is ready"

echo "Ensuring KV engine is enabled at secret/"
vault secrets enable -path=secret kv-v2 2>/dev/null || true

echo "Checking if JWT_SECRET already exists"

if vault kv get secret/kv/datashare-backend >/dev/null 2>&1
then
  echo "JWT_SECRET already exists"
else
  echo "Generating JWT secret"

  JWT=$(head -c 32 /dev/urandom | xxd -p)

  echo "Generated JWT_SECRET: $JWT"

  vault kv put secret/kv/datashare-backend JWT_SECRET="$JWT"

  echo "JWT_SECRET stored in Vault"
fi

echo "Vault initialization finished"