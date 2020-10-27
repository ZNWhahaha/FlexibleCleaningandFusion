# FlexibleCleaningandFusion
灵活清洗融合程序

插件：    
AssignmentHbaseID     <=>   向对应HBase表中插入ID属性    
FromPAPERToNew        <=>   将旧HBase表中数据转换格式并存储在新表中     
newBaiduMonkey        <=>   向对应HBase表中工具公司、机构名称建立地域属性

模块：   
VariableXmlParsing          <=>   XML转存HBase模块   
FlexibleCleaningModule1.1   <=>   数据清洗模块   
HbaseDataFusion             <=>   数据融合模块   
HbaseDataAssociation        <=>   数据关联模块   
FlexibleFromHbaseToNeo4j    <=>   HBase转存Neo4j模块    

执行顺序：   
VariableXmlParsing => FlexibleCleaningModule1.1 => HbaseDataFusion => AssignmentHbaseID => newBaiduMonkey => HbaseDataAssociation => FlexibleFromHbaseToNeo4j
