import org.json.JSONObject

data class ModelLinks (
    val pageName: String,
    val pageLink: String
)

data class DefaultMessageObject (
    val title: String,
    val message: String
)

data class MyProgramObject (
    val rowIndex: Int,
    val colective: String,
    val regional: String,
    val observations: String,
    val city: String,
    val creationDate: String,
    val onlyDate: String,
    val onlyHour: String,
    val client: String,
    val systemJJ: String,
    val systemOM: String,
    val system: String,
    val companyAT: String,
    val specialist: String,
    val patientName: String,
    val pagador: String,
    val lineNeg: String,
    val house: String,
    val patientCC: String,
    val appoitmentNumber: String,
    val requestNumber: String,
    val monthOne: String,
    val urgenceHours: String,
    val coberture: String,
    val confirmEmailHour: String,
    val planning: String,
    val externSupport: String,
    val responsibleEmail: String,
    val responsibleName: String,
    val assignDate: String,
    val assignStatus: String,
    val assignAuthorize: String,
    val realArriveDate: String,
    val arriveDate: String,
    val arriveLocation: String,
    val realExitDate: String,
    val exitDate: String,
    val exitLocation: String,
    val supportImage: String,
    val supportDoc: String,
    val technicalName: String,
    val remissionNumber: String,
    val processStatus: String,
    val typeCX: String,
    val date: String,
    val initialHour: String,
    val initialHourCX: String,
    val cancelMotive: String,
    val otherCancelMotive: String,
    val finishHourCX: String,
    val generalComments: String,
    val abourtType: String,
    val patientNameConfirm: String,
    val prequirurgico: String,
    val motiveCXConsume: String,
    val remissionStatus: String,
    val perfectProcess: String
){
    override fun toString(): String{

        // creamos lista valida
        val mutableMap = mutableMapOf<String, Any>();
        mutableMap.put("rowIndex", rowIndex);
        mutableMap.put("colective", colective);
        mutableMap.put("regional", regional);
        mutableMap.put("observations", observations);
        mutableMap.put("city", city);
        mutableMap.put("creationDate", creationDate);
        mutableMap.put("onlyDate", onlyDate);
        mutableMap.put("onlyHour", onlyHour);
        mutableMap.put("client", client);
        mutableMap.put("systemJJ", systemJJ);
        mutableMap.put("systemOM", systemOM);
        mutableMap.put("system", system);
        mutableMap.put("companyAT", companyAT);
        mutableMap.put("specialist", specialist);
        mutableMap.put("patientName", patientName);
        mutableMap.put("pagador", pagador);
        mutableMap.put("lineNeg", lineNeg);
        mutableMap.put("house", house);
        mutableMap.put("patientCC", patientCC);
        mutableMap.put("appoitmentNumber", appoitmentNumber);
        mutableMap.put("requestNumber", requestNumber);
        mutableMap.put("monthOne", monthOne);
        mutableMap.put("urgenceHours", urgenceHours);
        mutableMap.put("coberture", coberture);
        mutableMap.put("confirmEmailHour", confirmEmailHour);
        mutableMap.put("planning", planning);
        mutableMap.put("externSupport", externSupport);
        mutableMap.put("responsibleEmail", responsibleEmail);
        mutableMap.put("responsibleName", responsibleName);
        mutableMap.put("assignDate", assignDate);
        mutableMap.put("assignStatus", assignStatus);
        mutableMap.put("assignAuthorize", assignAuthorize);
        mutableMap.put("realArriveDate", realArriveDate);
        mutableMap.put("arriveDate", arriveDate);
        mutableMap.put("arriveLocation", arriveLocation);
        mutableMap.put("realExitDate", realExitDate);
        mutableMap.put("exitDate", exitDate);
        mutableMap.put("exitLocation", exitLocation);
        mutableMap.put("supportImage", supportImage);
        mutableMap.put("supportDoc", supportDoc);
        mutableMap.put("technicalName", technicalName);
        mutableMap.put("remissionNumber", remissionNumber);
        mutableMap.put("processStatus", processStatus);
        mutableMap.put("typeCX", typeCX);
        mutableMap.put("date", date);
        mutableMap.put("initialHour", initialHour);
        mutableMap.put("initialHourCX", initialHourCX);
        mutableMap.put("cancelMotive", cancelMotive);
        mutableMap.put("otherCancelMotive", otherCancelMotive);
        mutableMap.put("finishHourCX", finishHourCX);
        mutableMap.put("generalComments", generalComments);
        mutableMap.put("abourtType", abourtType);
        mutableMap.put("patientNameConfirm", patientNameConfirm);
        mutableMap.put("prequirurgico", prequirurgico);
        mutableMap.put("motiveCXConsume", motiveCXConsume);
        mutableMap.put("remissionStatus", remissionStatus);
        mutableMap.put("perfectProcess", perfectProcess);

        // retornamos el dato como string
        return JSONObject(mutableMap as Map<*, *>?).toString();
    }
}

data class UserObject (
    val rowIndex: Int,
    val userId: String,
    val userName: String,
    val userMail: String,
    val userStatus: String,
    val userProfile: String,
    val userType: String
){
    override fun toString(): String{

        // creamos lista valida
        val mutableMap = mutableMapOf<String, Any>();
        mutableMap.put("rowIndex", rowIndex);
        mutableMap.put("userId", userId);
        mutableMap.put("userName", userName);
        mutableMap.put("userMail", userMail);
        mutableMap.put("userStatus", userStatus);
        mutableMap.put("userProfile", userProfile);
        mutableMap.put("userType", userType);

        // retornamos el dato como string
        return JSONObject(mutableMap as Map<*, *>?).toString();
    }
}

data class LocationObject (
    val date: String,
    val fullName: String,
    val email: String,
    val latLong: String,
    val address: String
)