interface IDbFormModel {
    createdDate: String,
    updatedDate: String,
    updatedTime: String,
    formStatus: string,
    extraKeys: any,
    formData: any,
    formSubmissionId: Number,
    uniqueId: String,
    formDataHead?:{},
    image:string,
    checked?:boolean,
    attachmentCount:number
}