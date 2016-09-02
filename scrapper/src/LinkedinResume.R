library (rvest)
library (stringi)
library(selectr)

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
  
  name <- page_html %>% html_nodes("#name") %>% html_text()
  location <- page_html %>% html_nodes("#recommendations") %>% html_text()
  num_connections <- page_html %>% html_nodes(".member-connections strong") %>% html_text()
  experience <- page_html %>% html_nodes("#background-experience") %>% html_text()
  education <- page_html %>% html_nodes("#background-education") %>% html_text()
  recommendations <- page_html %>% html_nodes("#endorsements") %>% html_text()
  summary <- page_html %>% html_nodes("#summary-item-view") %>% html_text()
  skills_nodes <- page_html %>% html_nodes("#profile-skills .skill-pill")
  skills <-
    lapply(skills_nodes, function(node) {
      num <- node %>% html_nodes(".num-endorsements") %>% html_text()
      name <- node %>% html_nodes(".endorse-item-name-text") %>% html_text()
      data.frame(name = name, num = num)
    })
  
  skills <- do.call(rbind, skills)
  
  list(
    name = name,
    location = location,
    num_connections = num_connections,
    experience = experience,
    education = education,
    recommendations = recommendations,
    summary = summary,
    skills = skills
  ) 
}

input_file = "/Users/sagraw200/Documents/dev/team-personal/r_projects/scrapper/resources/ids/ids.txt"
output_folder = "/Users/sagraw200/Documents/dev/team-personal/r_projects/scrapper/resources/data_fields/"

list <- read.delim(input_file, header=FALSE, stringsAsFactors=FALSE)
names(list)<-c("urls")

pgsession = login_linkedin()

for(i in seq(nrow(list))) {
  print(list$urls[i])
  filename <- paste0(output_folder, stri_sub(list$urls[i],from = 29, to = -1) , ".txt")
  print(filename)
  text <- scrape_linkedin(pgsession, list$urls[i])
  # write.csv(data.frame(text), filename)
  sink(file = paste(filename)) # open file to write
  print(text)
  sink() # close the file
  
}
