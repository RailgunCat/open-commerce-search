# BulkImportData

Composite object that is used to add documents to the index.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
| **documents** | [**List**](Document.md) | An array of Documents or Products that should be indexed into the given index.Products have the speciality to contain other documents as variants. | [default to null] |
| **session** | [**ImportSession**](ImportSession.md) | Import session information that were retrieved by the startImport method. | required |

[[Back to Model list]](../index.md#documentation-for-models) [[Back to API list]](../index.md#documentation-for-api-endpoints) [[Back to README]](../index.md)

