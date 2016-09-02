library (rvest)
library (stringi)
library (selectr)

login_linkedin <- function() {
  username <- ""
  password <- ""
  linkedin_url <- "http://linkedin.com/"
  
  pgsession <- html_session(linkedin_url) 
  pgform <- html_form(pgsession)[[1]]
  filled_form <- set_values(pgform, session_key = username,  session_password = password)
  submit_form(pgsession, filled_form)
  return (pgsession)
}
scrape_linkedin <- function(pgsession, user_url) {
  pgsession <- jump_to(pgsession, user_url)
  page_html <- read_html(pgsession)
  return (page_html)
}

args <- commandArgs(TRUE)
start <- args[1]
end <- args[2]

input_file = "/Users/sagraw200/Documents/dev/team-personal/r_projects/scrapper/resources/ids/user.data"
output_folder = "/Users/sagraw200/Documents/dev/team-personal/r_projects/scrapper/resources/data_html/"

list <- read.delim(input_file, header=FALSE, stringsAsFactors=FALSE)

start <- as.integer(start)
end <- as.integer(end)
sublist <- as.data.frame(list$V1[start:end],stringsAsFactors =FALSE)
names(sublist)<-c("urls")

pgsession = login_linkedin()

for(i in seq(nrow(sublist))) {
  callthis <- sublist$urls[i]
  text <- scrape_linkedin(pgsession, callthis)
  filename <- paste0(output_folder, stri_sub(callthis , from = 29, to = -1) , ".txt")
  write.csv(toString(text), file=filename)
}




