#!/bin/sh

set -e

echo "Creating Glue Schema Registry..."
aws glue create-registry --registry-name local-registry --endpoint-url http://schemaregistry:3000

echo "Registering Avro schemas..."
for file in /schemas/*.avsc; do
  schema_name=$(basename "$file" .avsc)
  schema_definition=$(cat "$file" | sed ':a;N;$!ba;s/\n/ /g')  # Convert newlines to spaces

  echo "Registering schema: $schema_name"
  aws glue create-schema \
    --registry-id RegistryName=local-registry \
    --schema-name "$schema_name" \
    --data-format AVRO \
    --compatibility FULL \
    --schema-definition "$schema_definition" \
    --endpoint-url http://schemaregistry:3000
done

echo "Schema registration completed successfully."
