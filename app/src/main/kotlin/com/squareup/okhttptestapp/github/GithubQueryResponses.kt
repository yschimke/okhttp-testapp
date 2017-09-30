package com.squareup.okhttptestapp.github

import com.apollographql.apollo.api.Response
import com.squareup.okhttptestapp.github.IssuesQuery.Data

fun Response<Data>.issues(): List<String> =
    this.data()?.repository()?.issues()?.nodes()?.map { it.fragments().issueFragment().title() }.orEmpty()