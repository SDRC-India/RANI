interface IDbFormModel {
    createdDate: String,
    updatedDate: String,
    updatedTime: String,
    formStatus: string,
    formData: any,
    formId: any, 
    uniqueId: String,
    formDataHead?:{},
    image:string,
    checked?:boolean,
    attachmentCount:number
}