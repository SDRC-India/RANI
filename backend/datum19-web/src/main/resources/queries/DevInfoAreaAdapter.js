db.getCollection('area_devinfo').aggregate([
{
    $lookup : {
        from : 'areaLevel',
        foreignField : 'areaLevelId',
        localField : 'Area_Level',
        as : 'areaLevel'
        }
    },
    { $unwind : {path : '$areaLevel'}},
{
    $project : {
        _class : "org.sdrc.rani.document.Area",
        areaId : "$Area_NId",
        areaName : "$Area_Name",
        areaCode : "$Area_ID",
        parentAreaId : "$Area_Parent_NId",
        live : true,
        areaLevel : "$areaLevel"
        }
    }, 
    { $out : 'area'}
])