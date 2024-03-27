plugins {
   pureKotlinModule
   sqldelight
}

sqldelight {
   databases {
      create("Database") {
         packageName.set("com.matejdro.mbus.sqldelight.generated")
         schemaOutputDirectory.set(file("src/main/sqldelight/databases"))
      }
   }
}

dependencies {
}
