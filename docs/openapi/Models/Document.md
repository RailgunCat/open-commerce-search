# Document

A data record that contains any data relevant for search.
The single field types and conversions are part of the according service configuration.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
| **id** | **String** |  | required |
| **attributes** | [**List<Attribute>**](Attribute.md) | multiple attributes can be delivered separately from standard data fields | [optional] [default to null] |
| **categories** | [**List<Category[]>**](Category.md) | A category path is a list of Category objects that are defined in a hierarchical parent-child relationship.Multiple category paths can be defined per document, therefor this property is a list of category arrays. | [optional] [default to null] |
| **data** | [**Map**](Document_data_value.md) | The data property should be used for standard fields, such as title, description, price. Only values of the following types are accepted (others will be dropped silently): Standard primitive types (Boolean, String, Integer, Double) and arrays of these types. Attributes (key-value objects with ID) should be passed to the attributes property. | [default to null] |

[[Back to Model list]](../index.md#documentation-for-models) [[Back to API list]](../index.md#documentation-for-api-endpoints) [[Back to README]](../index.md)

