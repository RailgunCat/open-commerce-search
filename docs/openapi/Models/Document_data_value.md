# Document_data_value

The data property should be used for standard fields, such as title, description, price.
Only values of the following types are accepted (others will be dropped silently):
Standard primitive types (Boolean, String, Integer, Double) and arrays of these types.
Attributes (key-value objects with ID) should be passed to the attributes property.

## Properties

| Name | Type      | Description                         | Notes                        |
|----- | --------- | ----------------------------------- | ---------------------------- |
| * | boolean      | usable for filterable fields        | example: isAvailable         |
| * | integer/long | mostly usable for attributes or IDs | example: size, ean           |
| * | double/float | usable for pricing or detailed attributes | example: price, weight, score-value |
| * | string       | most data comes as string. usable for searchable data or facets and filters | example: brand, title |

[[Back to Model list]](../index.md#documentation-for-models) [[Back to API list]](../index.md#documentation-for-api-endpoints) [[Back to README]](../index.md)

